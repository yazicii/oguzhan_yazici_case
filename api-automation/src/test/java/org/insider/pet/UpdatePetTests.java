package org.insider.pet;

import io.qameta.allure.*;
import org.insider.BaseApiTest;
import org.insider.client.PetApiClient;
import org.insider.models.Pet;
import org.insider.models.Tag;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;

@Feature("Pet API")
@Story("Update Pet")
public class UpdatePetTests extends BaseApiTest {

    private PetApiClient petApi;
    private long petId;

    @BeforeClass(alwaysRun = true)
    public void initClient() {
        petApi = new PetApiClient(spec);
    }

    private String seedName;
    private String seedStatus;
    private long seedCategoryId;
    private String seedCategoryName;
    private List<String> seedPhotoUrls;
    private long seedTagId;
    private String seedTagName;

    @BeforeMethod(alwaysRun = true)
    public void seedPet() {
        petId = uniquePetId();
        seedName = "OriginalName";
        seedStatus = "available";
        seedCategoryId = 1L;
        seedCategoryName = "Dogs";
        seedPhotoUrls = List.of("https://example.com/original.jpg");
        seedTagId = 1L;
        seedTagName = "calm";

        Pet seed = Pet.builder()
                .id(petId)
                .name(seedName)
                .status(seedStatus)
                .category(seedCategoryId, seedCategoryName)
                .photoUrls(seedPhotoUrls)
                .tags(List.of(new Tag(seedTagId, seedTagName)))
                .build();
        petApi.createPet(seed)
                .then()
                .statusCode(200);
    }

    // ── POSITIVE SCENARIOS ──

    @Test(description = "Update pet name")
    @Severity(SeverityLevel.CRITICAL)
    @Description("PUT /pet — Update pet name")
    public void updatePetName() {
        String newName = "UpdatedName";
        String expectedStatus = seedStatus;

        Pet updated = Pet.builder()
                .id(petId)
                .name(newName)
                .status(expectedStatus)
                .photoUrls(seedPhotoUrls)
                .build();

        petApi.updatePet(updated)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) petId))
                .body("name", equalTo(newName))
                .body("status", equalTo(expectedStatus))
                .body("photoUrls", notNullValue());

        petApi.getPetById(petId)
                .then()
                .statusCode(200)
                .body("name", equalTo(newName));
    }

    @Test(description = "Update pet status from available to sold")
    @Severity(SeverityLevel.CRITICAL)
    @Description("PUT /pet — Update status from available to sold")
    public void updatePetStatus() {
        String newStatus = "sold";

        Pet updated = Pet.builder()
                .id(petId)
                .name(seedName)
                .status(newStatus)
                .photoUrls(seedPhotoUrls)
                .build();

        petApi.updatePet(updated)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) petId))
                .body("name", equalTo(seedName))
                .body("status", equalTo(newStatus))
                .body("photoUrls", notNullValue());
    }

    @Test(description = "Update pet category and tags")
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /pet — Update category and tags")
    public void updatePetCategoryAndTags() {
        long newCategoryId = 2L;
        String newCategoryName = "Cats";
        String tag1Name = "playful";
        String tag2Name = "indoor";

        Pet updated = Pet.builder()
                .id(petId)
                .name(seedName)
                .status(seedStatus)
                .category(newCategoryId, newCategoryName)
                .photoUrls(seedPhotoUrls)
                .tags(List.of(new Tag(2L, tag1Name), new Tag(3L, tag2Name)))
                .build();

        petApi.updatePet(updated)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) petId))
                .body("category.id", equalTo((int) newCategoryId))
                .body("category.name", equalTo(newCategoryName))
                .body("tags.size()", equalTo(2))
                .body("tags[0].name", equalTo(tag1Name))
                .body("tags[1].name", equalTo(tag2Name));
    }

    @Test(description = "Full update — change all fields at once")
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /pet — Update all fields at once")
    public void fullUpdateAllFields() {
        String newName = "CompletelyNew";
        String newStatus = "pending";
        String newCategoryName = "Birds";
        List<String> newPhotoUrls = List.of("https://example.com/new1.jpg", "https://example.com/new2.jpg");
        String newTagName = "exotic";

        Pet updated = Pet.builder()
                .id(petId)
                .name(newName)
                .status(newStatus)
                .category(3L, newCategoryName)
                .photoUrls(newPhotoUrls)
                .tags(List.of(new Tag(4L, newTagName)))
                .build();

        petApi.updatePet(updated)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) petId))
                .body("name", equalTo(newName))
                .body("status", equalTo(newStatus))
                .body("category.name", equalTo(newCategoryName))
                .body("photoUrls.size()", equalTo(newPhotoUrls.size()))
                .body("tags[0].name", equalTo(newTagName));
    }

    // ── NEGATIVE SCENARIOS ──

    @Test(description = "Update pet with non-existent ID")
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /pet — Update with non-existent ID (Petstore returns 200 and creates new)")
    public void updateNonExistentPet() {
        long nonExistentId = 888888888L;
        String name = "GhostPet";
        String status = "available";
        List<String> photoUrls = List.of("https://example.com/ghost.jpg");

        Pet ghost = Pet.builder()
                .id(nonExistentId)
                .name(name)
                .status(status)
                .photoUrls(photoUrls)
                .build();

        petApi.updatePet(ghost)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test(description = "Update pet with invalid JSON body")
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /pet — Update with invalid JSON body")
    public void updatePetWithInvalidBody() {
        petApi.updatePetWithRawBody("{broken json!}")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(405), equalTo(500)));
    }
}
