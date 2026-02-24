package org.insider.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pet {

    private Long id;
    private Category category;
    private String name;
    private List<String> photoUrls;
    private List<Tag> tags;
    private String status; // available, pending, sold

    public Pet() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }

    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Pet{id=" + id + ", name='" + name + "', status='" + status + "'}";
    }

    /** Builder pattern for fluent Pet creation in tests. */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Pet pet = new Pet();

        public Builder id(Long id) { pet.setId(id); return this; }
        public Builder name(String name) { pet.setName(name); return this; }
        public Builder status(String status) { pet.setStatus(status); return this; }
        public Builder category(Long id, String name) { pet.setCategory(new Category(id, name)); return this; }
        public Builder photoUrls(List<String> urls) { pet.setPhotoUrls(urls); return this; }
        public Builder tags(List<Tag> tags) { pet.setTags(tags); return this; }

        public Pet build() { return pet; }
    }
}
