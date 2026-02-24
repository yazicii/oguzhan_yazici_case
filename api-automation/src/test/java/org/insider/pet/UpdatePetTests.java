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

    @BeforeMethod(alwaysRun = true)
    public void seedPet() {
        petId = uniquePetId();
        Pet seed = Pet.builder()
                .id(petId)
                .name("OriginalName")
                .status("available")
                .category(1L, "Dogs")
                .photoUrls(List.of("https://example.com/original.jpg"))
                .tags(List.of(new Tag(1L, "calm")))
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
        Pet updated = Pet.builder()
                .id(petId)
                .name("UpdatedName")
                .status("available")
                .photoUrls(List.of("https://example.com/original.jpg"))
                .build();

        petApi.updatePet(updated)
                .then()
                .statusCode(200)
                .body("name", equalTo("UpdatedName"));

        petApi.getPetById(petId)
                .then()
                .statusCode(200)
                .body("name", equalTo("UpdatedName"));
    }

    @Test(description = "Update pet status from available to sold")
    @Severity(SeverityLevel.CRITICAL)
    @Description("PUT /pet — Update status from available to sold")
    public void updatePetStatus() {
        Pet updated = Pet.builder()
                .id(petId)
                .name("OriginalName")
                .status("sold")
                .photoUrls(List.of("https://example.com/original.jpg"))
                .build();

        petApi.updatePet(updated)
                .then()
                .statusCode(200)
                .body("status", equalTo("sold"));
    }

    @Test(description = "Update pet category and tags")
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /pet — Update category and tags")
    public void updatePetCategoryAndTags() {
        Pet updated = Pet.builder()
                .id(petId)
                .name("OriginalName")
                .status("available")
                .category(2L, "Cats")
                .photoUrls(List.of("https://example.com/original.jpg"))
                .tags(List.of(new Tag(2L, "playful"), new Tag(3L, "indoor")))
                .build();

        petApi.updatePet(updated)
                .then()
                .statusCode(200)
                .body("category.name", equalTo("Cats"))
                .body("tags.size()", equalTo(2))
                .body("tags[0].name", equalTo("playful"));
    }

    @Test(description = "Full update — change all fields at once")
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /pet — Update all fields at once")
    public void fullUpdateAllFields() {
        Pet updated = Pet.builder()
                .id(petId)
                .name("CompletelyNew")
                .status("pending")
                .category(3L, "Birds")
                .photoUrls(List.of("https://example.com/new1.jpg", "https://example.com/new2.jpg"))
                .tags(List.of(new Tag(4L, "exotic")))
                .build();

        petApi.updatePet(updated)
                .then()
                .statusCode(200)
                .body("name", equalTo("CompletelyNew"))
                .body("status", equalTo("pending"))
                .body("category.name", equalTo("Birds"));
    }

    // ── NEGATIVE SCENARIOS ──

    @Test(description = "Update pet with non-existent ID")
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /pet — Update with non-existent ID (Petstore returns 200 and creates new)")
    public void updateNonExistentPet() {
        Pet ghost = Pet.builder()
                .id(888888888L)
                .name("GhostPet")
                .status("available")
                .photoUrls(List.of("https://example.com/ghost.jpg"))
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
