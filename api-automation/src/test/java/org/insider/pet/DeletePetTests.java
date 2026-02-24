package org.insider.pet;

import io.qameta.allure.*;
import org.insider.BaseApiTest;
import org.insider.client.PetApiClient;
import org.insider.models.Pet;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;

@Feature("Pet API")
@Story("Delete Pet")
public class DeletePetTests extends BaseApiTest {

    private PetApiClient petApi;

    @BeforeClass(alwaysRun = true)
    public void initClient() {
        petApi = new PetApiClient(spec);
    }

    private void createPetForDeletion(long petId, String name) {
        Pet pet = Pet.builder()
                .id(petId)
                .name(name)
                .status("available")
                .photoUrls(List.of("https://example.com/del.jpg"))
                .build();
        petApi.createPet(pet)
                .then()
                .statusCode(200);
    }

    // ── POSITIVE SCENARIOS ──

    @Test(description = "Delete an existing pet and verify it's gone")
    @Severity(SeverityLevel.CRITICAL)
    @Description("DELETE /pet/{petId} — Delete existing pet and verify it returns 404")
    public void deleteExistingPet() {
        long id = uniquePetId();
        createPetForDeletion(id, "ToBeDeleted");

        petApi.deletePet(id)
                .then()
                .statusCode(200);

        petApi.getPetById(id)
                .then()
                .statusCode(404);
    }

    @Test(description = "Delete pet and verify GET returns 'Pet not found'")
    @Severity(SeverityLevel.CRITICAL)
    @Description("DELETE /pet/{petId} — After deletion, GET returns 'Pet not found' message")
    public void deletePetVerifyNotFoundMessage() {
        long petId = uniquePetId();
        createPetForDeletion(petId, "DeleteMessageTest");

        petApi.deletePet(petId)
                .then()
                .statusCode(200);

        petApi.getPetById(petId)
                .then()
                .statusCode(404)
                .body("message", equalTo("Pet not found"));
    }

    @Test(description = "Create, delete, recreate — full lifecycle")
    @Severity(SeverityLevel.NORMAL)
    @Description("Create → delete → recreate — full lifecycle test")
    public void createDeleteRecreateLifecycle() {
        long petId = uniquePetId();

        createPetForDeletion(petId, "LifecyclePet");

        petApi.deletePet(petId)
                .then()
                .statusCode(200);

        petApi.getPetById(petId)
                .then()
                .statusCode(404);

        createPetForDeletion(petId, "LifecyclePetReborn");

        petApi.getPetById(petId)
                .then()
                .statusCode(200)
                .body("name", equalTo("LifecyclePetReborn"));
    }

    // ── NEGATIVE SCENARIOS ──

    @Test(description = "Delete a non-existent pet")
    @Severity(SeverityLevel.NORMAL)
    @Description("DELETE /pet/{petId} — Expect 404 when deleting non-existent pet")
    public void deleteNonExistentPet() {
        petApi.deletePet(777777777L)
                .then()
                .statusCode(404);
    }

    @Test(description = "Delete the same pet twice")
    @Severity(SeverityLevel.NORMAL)
    @Description("DELETE /pet/{petId} — Expect 404 on second deletion of the same pet")
    public void deleteSamePetTwice() {
        long petId = uniquePetId();
        createPetForDeletion(petId, "DoubleDel");

        petApi.deletePet(petId)
                .then()
                .statusCode(200);

        petApi.deletePet(petId)
                .then()
                .statusCode(404);
    }
}
