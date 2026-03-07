package org.insider.pet;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.insider.BaseApiTest;
import org.insider.client.PetApiClient;
import org.insider.models.Pet;
import org.insider.models.Tag;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;

@Feature("Pet API")
@Story("Create Pet")
public class CreatePetTests extends BaseApiTest {

    private PetApiClient petApi;

    @BeforeClass(alwaysRun = true)
    public void initClient() {
        petApi = new PetApiClient(spec);
    }

    // ── POSITIVE SCENARIOS ──

    @Test(description = "Create a pet with all fields populated")
    @Severity(SeverityLevel.CRITICAL)
    @Description("POST /pet — All fields populated: id, name, category, photoUrls, tags, status")
    public void createPetWithAllFields() {
        long id = uniquePetId();
        String name = "Buddy";
        String status = "available";
        long categoryId = 1L;
        String categoryName = "Dogs";
        List<String> photoUrls = List.of("https://example.com/buddy.jpg");
        long tagId = 1L;
        String tagName = "friendly";

        Pet pet = Pet.builder()
                .id(id)
                .name(name)
                .status(status)
                .category(categoryId, categoryName)
                .photoUrls(photoUrls)
                .tags(List.of(new Tag(tagId, tagName)))
                .build();

        petApi.createPet(pet)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) id))
                .body("name", equalTo(name))
                .body("status", equalTo(status))
                .body("category.id", equalTo((int) categoryId))
                .body("category.name", equalTo(categoryName))
                .body("photoUrls", notNullValue())
                .body("tags[0].name", equalTo(tagName));
    }

    @Test(description = "Create a pet with only required fields")
    @Severity(SeverityLevel.CRITICAL)
    @Description("POST /pet — Create with only name and photoUrls")
    public void createPetWithRequiredFieldsOnly() {
        String name = "MinimalPet";
        List<String> photoUrls = List.of("https://example.com/minimal.jpg");

        Pet pet = Pet.builder()
                .name(name)
                .photoUrls(photoUrls)
                .build();

        petApi.createPet(pet)
                .then()
                .statusCode(200)
                .body("name", equalTo(name))
                .body("id", notNullValue())
                .body("photoUrls", notNullValue());
    }

    @Test(description = "Create a pet with 'pending' status")
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /pet — Create with status=pending")
    public void createPetWithPendingStatus() {
        long id = uniquePetId();
        String name = "PendingPet";
        String status = "pending";
        List<String> photoUrls = List.of("https://example.com/pending.jpg");

        Pet pet = Pet.builder()
                .id(id)
                .name(name)
                .status(status)
                .photoUrls(photoUrls)
                .build();

        petApi.createPet(pet)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", equalTo(name))
                .body("status", equalTo(status))
                .body("photoUrls", notNullValue());
    }

    @Test(description = "Create a pet with 'sold' status")
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /pet — Create with status=sold")
    public void createPetWithSoldStatus() {
        long id = uniquePetId();
        String name = "SoldPet";
        String status = "sold";
        List<String> photoUrls = List.of("https://example.com/sold.jpg");

        Pet pet = Pet.builder()
                .id(id)
                .name(name)
                .status(status)
                .photoUrls(photoUrls)
                .build();

        petApi.createPet(pet)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", equalTo(name))
                .body("status", equalTo(status))
                .body("photoUrls", notNullValue());
    }

    // ── NEGATIVE SCENARIOS ──

    @Test(description = "Create a pet with invalid JSON body")
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /pet — Expect error when invalid JSON is sent")
    public void createPetWithInvalidJson() {
        Response response = petApi.createPetWithRawBody("{invalid json!!}");

        response.then()
                .statusCode(anyOf(equalTo(400), equalTo(405), equalTo(500)));
    }

    @Test(description = "Create a pet with empty body — expect validation error")
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /pet — API should reject empty body (missing required name, photoUrls)")
    public void createPetWithEmptyBody() {
        Response response = petApi.createPetWithRawBody("{}");

        response.then()
                .statusCode(anyOf(equalTo(400), equalTo(405), equalTo(422)));
    }

    @Test(description = "Create a pet with negative ID — expect validation error")
    @Severity(SeverityLevel.MINOR)
    @Description("POST /pet — API should reject negative id")
    public void createPetWithNegativeId() {
        long invalidId = -1L;
        String name = "NegativeIdPet";
        List<String> photoUrls = List.of("https://example.com/neg.jpg");

        Pet pet = Pet.builder()
                .id(invalidId)
                .name(name)
                .photoUrls(photoUrls)
                .build();

        Response response = petApi.createPet(pet);

        response.then()
                .statusCode(anyOf(equalTo(400), equalTo(405), equalTo(422)));
    }

    @Test(description = "Create a pet with very long name")
    @Severity(SeverityLevel.MINOR)
    @Description("POST /pet — Create with very long name (1000 characters)")
    public void createPetWithVeryLongName() {
        String longName = "A".repeat(1000);
        List<String> photoUrls = List.of("https://example.com/long.jpg");

        Pet pet = Pet.builder()
                .name(longName)
                .photoUrls(photoUrls)
                .build();

        petApi.createPet(pet)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", equalTo(longName))
                .body("photoUrls", notNullValue());
    }
}
