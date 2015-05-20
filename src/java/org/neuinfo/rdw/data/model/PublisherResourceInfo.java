package org.neuinfo.rdw.data.model;

public class PublisherResourceInfo {
	private int id;
	String title;
	String publicationName;
	String genre;
	String resourceName;
	String description;
	String meshHeadings;
	private String label;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMeshHeadings() {
		return meshHeadings;
	}

	public void setMeshHeadings(String meshHeadings) {
		this.meshHeadings = meshHeadings;
	}

	public PublisherResourceInfo(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPublicationName() {
		return publicationName;
	}

	public void setPublicationName(String publicationName) {
		this.publicationName = publicationName;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getId() {
		return id;
	}
}
