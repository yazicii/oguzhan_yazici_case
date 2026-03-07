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
        Pet pet = Pet.builder()
                .id(id)
                .name("Buddy")
                .status("available")
                .category(1L, "Dogs")
                .photoUrls(List.of("https://example.com/buddy.jpg"))
                .tags(List.of(new Tag(1L, "friendly")))
                .build();

        petApi.createPet(pet)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) id))
                .body("name", equalTo("Buddy"))
                .body("status", equalTo("available"))
                .body("category.name", equalTo("Dogs"))
                .body("tags[0].name", equalTo("friendly"));
    }

    @Test(description = "Create a pet with only required fields")
    @Severity(SeverityLevel.CRITICAL)
    @Description("POST /pet — Create with only name and photoUrls")
    public void createPetWithRequiredFieldsOnly() {
        Pet pet = Pet.builder()
                .name("MinimalPet")
                .photoUrls(List.of("https://example.com/minimal.jpg"))
                .build();

        petApi.createPet(pet)
                .then()
                .statusCode(200)
                .body("name", equalTo("MinimalPet"))
                .body("id", notNullValue());
    }

    @Test(description = "Create a pet with 'pending' status")
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /pet — Create with status=pending")
    public void createPetWithPendingStatus() {
        Pet pet = Pet.builder()
                .id(uniquePetId())
                .name("PendingPet")
                .status("pending")
                .photoUrls(List.of("https://example.com/pending.jpg"))
                .build();

        petApi.createPet(pet)
                .then()
                .statusCode(200)
                .body("status", equalTo("pending"));
    }

    @Test(description = "Create a pet with 'sold' status")
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /pet — Create with status=sold")
    public void createPetWithSoldStatus() {
        Pet pet = Pet.builder()
                .id(uniquePetId())
                .name("SoldPet")
                .status("sold")
                .photoUrls(List.of("https://example.com/sold.jpg"))
                .build();

        petApi.createPet(pet)
                .then()
                .statusCode(200)
                .body("status", equalTo("sold"));
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
        Pet pet = Pet.builder()
                .id(-1L)
                .name("NegativeIdPet")
                .photoUrls(List.of("https://example.com/neg.jpg"))
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
        Pet pet = Pet.builder()
                .name(longName)
                .photoUrls(List.of("https://example.com/long.jpg"))
                .build();

        petApi.createPet(pet)
                .then()
                .statusCode(200)
                .body("name", equalTo(longName));
    }
}
