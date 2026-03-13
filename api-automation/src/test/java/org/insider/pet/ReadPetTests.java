package org.insider.pet;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.insider.BaseApiTest;
import org.insider.client.PetApiClient;
import org.insider.models.Pet;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;

@Feature("Pet API")
@Story("Read Pet")
public class ReadPetTests extends BaseApiTest {

    private static final String PET_NOT_FOUND_MESSAGE = "Pet not found";

    private PetApiClient petApi;
    private long existingPetId;
    private String existingPetName;
    private String existingPetStatus;
    private long existingCategoryId;
    private String existingCategoryName;
    private List<String> existingPhotoUrls;

    @BeforeClass(alwaysRun = true)
    public void initClient() {
        petApi = new PetApiClient(spec);
        existingPetId = uniquePetId();
        existingPetName = "ReadTestDog";
        existingPetStatus = "available";
        existingCategoryId = 1L;
        existingCategoryName = "Dogs";
        existingPhotoUrls = List.of("https://example.com/read.jpg");

        Pet seed = Pet.builder()
                .id(existingPetId)
                .name(existingPetName)
                .status(existingPetStatus)
                .category(existingCategoryId, existingCategoryName)
                .photoUrls(existingPhotoUrls)
                .build();
        Response createResponse = petApi.createPet(seed);
        createResponse.then().statusCode(200);
    }

    // ── POSITIVE SCENARIOS ──

    @Test(description = "Get pet by valid ID")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /pet/{petId} — Retrieve pet by existing ID")
    public void getPetByValidId() {
        petApi.getPetById(existingPetId)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) existingPetId))
                .body("name", equalTo(existingPetName))
                .body("status", equalTo(existingPetStatus))
                .body("category.id", equalTo((int) existingCategoryId))
                .body("category.name", equalTo(existingCategoryName))
                .body("photoUrls", notNullValue());
    }

    @Test(description = "Find pets by 'available' status")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /pet/findByStatus?status=available — Returns list of available pets")
    public void findPetsByAvailableStatus() {
        String status = "available";
        petApi.findPetsByStatus(status)
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue())
                .body("[0].name", notNullValue())
                .body("[0].status", equalTo(status))
                .body("[0].photoUrls", notNullValue());
    }

    @Test(description = "Find pets by 'pending' status")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /pet/findByStatus?status=pending — Returns list of pending pets")
    public void findPetsByPendingStatus() {
        String status = "pending";
        petApi.findPetsByStatus(status)
                .then()
                .statusCode(200)
                .body("findAll { it.status == '" + status + "' }.size()", greaterThanOrEqualTo(0));
    }

    @Test(description = "Find pets by 'sold' status")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /pet/findByStatus?status=sold — Returns list of sold pets")
    public void findPetsBySoldStatus() {
        String status = "sold";
        petApi.findPetsByStatus(status)
                .then()
                .statusCode(200)
                .body("findAll { it.status == '" + status + "' }.size()", greaterThanOrEqualTo(0));
    }

    // ── NEGATIVE SCENARIOS ──

    @Test(description = "Get pet by non-existent ID")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /pet/{petId} — Expect 404 for non-existent ID")
    public void getPetByNonExistentId() {
        long nonExistentId = 999999999L;
        petApi.getPetById(nonExistentId)
                .then()
                .statusCode(404)
                .body("message", equalTo(PET_NOT_FOUND_MESSAGE));
    }

    @Test(description = "Get pet by invalid (string) ID")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /pet/{petId} — Expect error for string ID ('abc')")
    public void getPetByInvalidStringId() {
        petApi.getPetByIdRaw("abc")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(404)));
    }

    @Test(description = "Get pet by zero ID")
    @Severity(SeverityLevel.MINOR)
    @Description("GET /pet/0 — Query with ID=0")
    public void getPetByZeroId() {
        petApi.getPetById(0L)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test(description = "Find pets by invalid status")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /pet/findByStatus?status=invalid — Expect empty list or error for invalid status")
    public void findPetsByInvalidStatus() {
        String invalidStatus = "invalidStatus";
        petApi.findPetsByStatus(invalidStatus)
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }
}
