package org.neuinfo.rdw

class CooccurrenceVizController {
    def cooccurrenceVizService
    // static allowedMethods = [getMostPopular: 'GET']
    def beforeInterceptor = [action: this.&auth]

    def auth() {
        if (!session.user) {
            redirect(controller: "User", action: 'login')
            return false;
        }
    }

    def index() {
        redirect(action: 'show')
    }

    def show() {
        println "in show"
        render(view: 'show', model: [])
    }


    def mostPopularToolCoocs() {
        println "in getMostPopular"
        String jsonStr = cooccurrenceVizService.prepareMostUsedResourcePairs(50)

        //FIXME For test
        //String jsonStr = new File('/home/bozyurt/dev/js/bjsplot/resources.json').getText('UTF-8')

        render(contentType: 'application/json', text: jsonStr, encoding: 'UTF-8')
    }

    def preparePairs4Resource(String registryName) {
        println "resourceName:$registryName"
        def list = Registry.executeQuery('select id from Registry where resourceName = :resourceName',
                [resourceName: registryName])
        assert list.size() == 1
        long registryId = list[0] as long
        String jsonStr = cooccurrenceVizService.preparePairs4Resource(registryId)
        render(contentType: 'application/json', text: jsonStr, encoding: 'UTF-8')
    }
}
