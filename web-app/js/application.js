if (typeof jQuery !== 'undefined') {
	(function($) {
		$('#spinner').ajaxStart(function() {
			$(this).fadeIn();
		}).ajaxStop(function() {
			$(this).fadeOut();
		});
      $.fn.fixedMenu = function() {
         return this.each(function() {
            var menu = $(this);
            menu.find('ul li > a').on('click', function(){
               if ($(this).parent().hasClass('active')) {
               $(this).parent().removeClass('active');
               } else {
                  $(this).parent().parent().find('.active').removeClass('active');
                  $(this).parent().addClass('active');
               }
            });
         });
      };
   })(jQuery);
}

var nif = nif || {};

nif.rdwModule = function() {
   function getButtonClass(selector) {
      var butClassNames = ['nosel', 'good', 'bad'];
      var idx, i, className = $(selector).attr('class'),
      classes = className.split(' ');
      for(i = 0; i < classes.length; i++) {
          idx = butClassNames.indexOf(classes[i]);
          if (idx != -1) {
              return butClassNames[idx];
          }
      }
      return null;
   }

   function prepResourceTypesCB(cbSelector, saveUserAnotUrl) {

      $(cbSelector).change(function() {
         var urIdx = $(this).attr('id').substring(3);
         var selValue = $(this).val();
         var label = getButtonClass('#annot_' + urIdx); 
         //console.log('selValue:' + selValue);
         $.ajax({
            url: saveUserAnotUrl,
            data: {
               urId: urIdx,
               label:label,
               resourceType: selValue
            },
            success: function(data) {
               console.log('saved resource type');
            },
            error: function(request, status, error) {
               alert(error);
            }
         });
      });
   }

   function prepShowSiteContentButton(butSelector, showContentsUrl) {
      $('#dialog-content').dialog({ autoOpen: false,
          height:400, width:700, modal:true,
          buttons: { 
             "OK" : function() { $(this).dialog('close'); }
          }
       });
       $(butSelector).click(function() {
            var registryId = $(this).attr('id').substring(5);
            $.ajax({
                url:showContentsUrl,
                data: { registryId: registryId },
                success:function(data) {
                   console.log(data);
                   $('#contents-area').text(data.content);
                   $('#dlg-header').text(data.resourceName);
                   $('#dialog-content').dialog('open');
                },
                error: function(request, status, error) {
                   alert(error); 
                }
            });
       });
   }

   function prepSiteDiffButton(butSelector, showContentsUrl) {
      $('#dialog-diff').dialog({ autoOpen: false,
          height:400, width:700, modal:true,
          buttons: { 
             "OK" : function() { $(this).dialog('close'); }
          }
       });

       $(butSelector).click(function() {
          var registryId = $(this).attr('id').substring(5);
          var butEl$ = $(this);
          $.ajax({
              url:showContentsUrl,
              data: { registryId: registryId },
              success:function(data) {
                 console.log(data);
                 $('#original').text(data.origContent);
                 $('#latest').text(data.content);
                 $('#dlg-header').text(data.resourceName);
                 $('#dialog-diff').dialog('open');
                 //var targetEl$ = $('#dp');
                 // _prepPanel(targetEl$,data);
              },
              error: function(request, status, error) {
                 alert(error); 
              }
          });
       });
   } 

   function _prepPanel(targetEl, data) {
      $('#diff-panel').remove();
      var div$ = $('<div></div>').attr('id','diff-panel');
      div$.appendTo(targetEl);
      var cp$ = $("<div class='cp' style='margin-bottom:3px;'><input type='button' id='diff-close-but' value='Close'/></div>");
      cp$.appendTo(div$);
      var that = this;
      $('#diff-close-but').click(function(event) {
           event.preventDefault();
           $('#diff-panel').remove();  
      });
      var leftDiv$ = $("<div class='diff-left'></div>");
      var rightDiv$ = $("<div class='diff-right'></div>");
      div$.append(leftDiv$);
      div$.append(rightDiv$);

      var leftContentDiv$ = $("<div></div>").text(data.origContent);
      var rightContentDiv$ = $("<div></div>").text(data.content);
      leftDiv$.append(leftContentDiv$);
      rightDiv$.append(rightContentDiv$);
   }

   function prepToolsButton(butSelector, getResourcesUrl, saveDedupInfoUrl, getResourceNameUrl) {
       var acCache = {};
       $(butSelector).click(function() {
           var urIdx = $(this).attr('id').substring(6);
           $.ajax({
               url:getResourceNameUrl,
               data: {
                  urId: urIdx
               },
               success: function(data) {
                  var resourceName = data.resourceName;
                  if (resourceName) {
                     $('#resourceName').val(resourceName);     
                  } else {
                     $('#resourceName').val('');     
                  }
                  $('#dialog-tools').data('toolsData', {urIdx:urIdx}).dialog('open');
               },
               error: function(request, status, error) {
                   alert(error);
               }
           });
       });

       $('#dialog-tools').dialog({ autoOpen: false,
          height:200, width:400, modal:true,
          buttons: { 
             "OK" : function() {
                console.log('OK pressed');
                    var resourceName = $.trim($('#resourceName').val());
                    /*
                    if (resourceName.length == 0) {
                        alert('No resource name is provided!');
                        return;
                    }
                    */
                    var urId = $(this).data('toolsData').urIdx;
                    var that = this; 
                    $.ajax({
                        url:saveDedupInfoUrl,
                        type:'POST',
                        data: {
                           urId:urId,
                           resourceName: resourceName
                        },
                        success: function(data) {
                           console.log("saved deduplication info. " + data);
                           if (data.rc) {
                              alert("Associated the url with resource " + data.rc);
                           }
                           var el$ = $('#tools_' + urId); 
                           if (resourceName && resourceName.length > 0) {
                             el$.addClass('has-resource');
                           } else {
                             el$.removeClass('has-resource');
                           }
                           $(that).dialog('close');
                        },
                        error: function(request, status, error) {
                           var el$ = $('#tools_' + urId); 
                           el$.removeClass('has-resource');
                           alert(error);
                        }
                    });
             },
             "Cancel": function() {
                $(this).dialog('close');
             }
          }
          /*,
          open : function() {
             $(this).parents('.ui-dialog-buttonpane button:eq(0)').focus();
             $('#resourceName').on('keypress', function(e) {
                 var code = e.keyCode || e.which;
                 if (code === 13) {
                    $(this).find('.ui-dialog-buttonset button').eq(0).trigger('click'); 
                 }
             });
          }
          */
       });

       $('#resourceName').autocomplete({
             source: function(request, response) {
                 var term = request.term;
                 if (term in acCache) {
                     response( acCache[term]);
                     return;
                 }
                 $.getJSON(getResourcesUrl, request, function(data, status, xhr) {
                    acCache[term] = data;
                    response(data);
                 });
             },
             minLength: 2,
             select: function(event, ui) {
                  if (ui.item) {
                    console.log(ui.item);
                  } else {
                      console.log(this.value);
                      $(this).val('');
                  }
             }
       });
   }

   function prepNotesButton(butSelector, saveUserAnotUrl, getUserNoteUrl) { 
      $(butSelector).click(function() {
         var urIdx = $(this).attr('id').substring(6);
         var that = this;
         var label = getButtonClass('#annot_' + urIdx); 
         $.ajax({
            url: getUserNoteUrl,
            data: {
               urId: urIdx
            },
            success: function(data) {
               var notes = data.uai.notes;
               if (notes) {
                  $('#notesText').val(notes);
               } else {
                  $('#notesText').val('');
               }
               $('#dialog-notes').data('notesData',{label:label, urIdx:urIdx}).dialog('open');
            },
            error: function(request, status, error) {
               alert(error);
            }
         });
       });

       $('#dialog-notes').dialog({autoOpen: false, 
          height:400, width:600, modal:true,
           buttons: {
              "OK" : function() {
                 var notes = $('#notesText').val();
                 var data = $(this).data('notesData');
                 var urId = data.urIdx;
                 var that = this;
                 $.ajax({
                    url: saveUserAnotUrl,
                    data: {
                       urId: urId,
                       label: data.label,
                       notes: notes
                    },
                    success: function(data) {
                       console.log('saved user annot data');
                       var el$ = $('#notes_' + urId);
                       if (notes && notes.length > 0) {
                          el$.addClass('has-notes');
                       } else {
                          el$.removeClass('has-notes');
                       }
                       $(that).dialog('close');
                    },
                    error: function(request, status, error) {
                       var el$ = $('#notes_' + urId);
                       el$.removeClass('has-notes');
                       alert(error);
                    }
                 });
              },
              "Cancel": function() { $(this).dialog('close'); }
           }
       }); 
   }

   function prepNotesAndRedirectUrlButton(butSelector, saveUserAnotUrl, getUserNoteUrl) { 
      $(butSelector).click(function() {
         var rcId = $(this).attr('id').substring(6);
         var that = this;
         var label = getButtonClass('#annot_' + rcId); 
         $.ajax({
            url: getUserNoteUrl,
            data: {rcId: rcId },
            success: function(data) {
               var notes = data.result.notes,
                   redirectUrl = data.result.redirectUrl;
               if (notes) {
                  $('#notesText').val(notes);
               } else {
                  $('#notesText').val('');
               }
               if (redirectUrl) {
                   $('#redirectUrlText').val(notes);
               } else {
                  $('#redirectUrlText').val('');
               }

               $('#dialog-notes').data('notesData',{label:label, rcId:rcId}).dialog('open');
            },
            error: function(request, status, error) {
               alert(error);
            }
          });
      });

      $('#dialog-notes').dialog({autoOpen: false, 
         height:400, width:600, modal:true,
         buttons: {
            "OK" : function() {
                  var notes = $('#notesText').val(), data = $(this).data('notesData'),
                     rcId = data.rcId, that = this,
                     redirectUrl = $.trim($('#redirectUrlText').val());
                  var params = {  urId: rcId,
                     label: data.label,
                     notes: notes};
                  if (redirectUrl && redirectUrl.length > 0) {
                     params.redirectUrl = redirectUrl;
                  }
                       
                  $.ajax({
                     url: saveUserAnotUrl,
                     data: params,
                     success: function(data) {
                        console.log('saved user annot data');
                        var el$ = $('#notes_' + rcId);
                        if ((notes && notes.length > 0) || (redirectUrl && redirectUrl.length > 0)) {
                           el$.addClass('has-notes');
                        } else {
                           el$.removeClass('has-notes');
                        }
                        $(that).dialog('close');
                     },
                     error: function(request, status, error) {
                        var el$ = $('notes_' + rcId);
                        el$.removeClass('has-notes');
                        alert(error);
                     }
                  });
               },
               "Cancel": function() { $(this).dialog('close'); }
         }
       });
   }

   function prepUserOpButton(butSelector, saveUserAnotUrl) { 
      var butClassNames = ['nosel', 'good', 'bad'];
      $(butSelector).click(function() {
         var urIdx = $(this).attr('id').substring(6);
         $(this).toggleClass( function(i, className) {
            var idx, newClass, curIdx = -1, i, classes = className.split(' ');
            for(i = 0; i < classes.length; i++) {
               if ( (curIdx = butClassNames.indexOf(classes[i])) != -1) {
                  break;  
               }
            }
            idx = (curIdx  + 1) % butClassNames.length;

            console.log('idx:'+idx + " urIdx:" +urIdx + " className:" + className);
            $(this).removeClass(className);
            newClass = butClassNames[idx];
            $.ajax({
               url: saveUserAnotUrl,
               data: {
                  urId: urIdx,
                  label: newClass
               },
               success: function(data) {
                  console.log('saved user annot data');
               },
               error: function(request, status, error) {
                  alert(error);
               }
            });
            return butClassNames[idx] + " user-annot-but";
         });
      });   
   }
   
   function toggleTextExpansion(selector, maxSize) {
      maxSize = maxSize || 300;
      // td.description
      $(selector).each(function() {
         var fullText = $(this).text();
         $(this).data('fullText', fullText);
         if(fullText.length > maxSize) {
            var ae$ = $('<a>...More</a>').addClass('more');
            $(this).text( fullText.substring(0,maxSize) ).append(ae$);
         }
      });
              
      $('a.more').live('click',function(event) {
         event.preventDefault();
         var td$ = $(this).closest(selector);
         var fullText = td$.data('fullText');
         td$.text(fullText)
         $(this).remove();
         var ae$ = $('<a>...Less</a>').addClass('less');
         td$.append(ae$);
      });
              
      $('a.less').live('click',function(event) {
         event.preventDefault();
         var td$ = $(this).closest(selector);
         var fullText = td$.data('fullText');
         $(this).remove();
         if(fullText.length > maxSize) {
            var ae$ = $('<a>...More</a>').addClass('more');
            td$.text( fullText.substring(0,maxSize) ).append(ae$);
         }
      });
   }
   function escapeRegExp(str) {
      return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
   }

   function hilight(selector, selText) {
        var spn, startTxt, endTxt, idx, text = $(selector).text();
        if (!text) { return }
        spn = '<span class="hilight">' + selText + '</span>';
        idx = text.indexOf(selText);
        if (idx == -1 && selText.indexOf('http://') != -1) {
            selText =  selText.replace(/^http:\/\//,'');
            spn = '<span class="hilight">' + selText + '</span>';
            idx = text.indexOf(selText);
        }
        if (idx != -1) {
            startTxt = text.substring(0, idx);
            endTxt = text.substring(idx + selText.length);
            $(selector).html(startTxt + spn + endTxt);
        } 
   }

   function handleResourceFilter(divElem$, getResourcesUrl, minLen) {
      var acCache = {};
      $('#filterInput', divElem$).autocomplete({
            minLength:minLen ? minLen : 2,
            source: function(request, response) {
               var term = request.term;
               if (term in acCache) {
                   response( acCache[term]);
                   return;
               }
               var selFilterType = $(':selected', $('#filterTypeChooser')).val();
               request.filterType = selFilterType;
               $.getJSON(getResourcesUrl, request, 
                 function(data, status, xhr) {
                      acCache[term] = data;
                      response(data);
                 });
            },
            select: function(event, ui) {
                if (ui.item) {
                  console.log(ui.item);
                } else {
                   $(this).val('');
                }
            }
      });
   }




   return {
      prepUserOpButton: prepUserOpButton,
      toggleTextExpansion:toggleTextExpansion,
      hilight:hilight,
      prepNotesButton: prepNotesButton,
      prepResourceTypesCB: prepResourceTypesCB,
      prepToolsButton: prepToolsButton,
      handleResourceFilter: handleResourceFilter,
      prepSiteDiffButton: prepSiteDiffButton,
      prepShowSiteContentButton : prepShowSiteContentButton,
      prepNotesAndRedirectUrlButton: prepNotesAndRedirectUrlButton 
   };

}();
