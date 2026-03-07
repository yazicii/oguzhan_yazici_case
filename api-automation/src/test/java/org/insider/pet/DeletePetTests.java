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

    private static final String PET_NOT_FOUND_MESSAGE = "Pet not found";

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
        String name = "ToBeDeleted";
        createPetForDeletion(id, name);

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
        String name = "DeleteMessageTest";
        createPetForDeletion(petId, name);

        petApi.deletePet(petId)
                .then()
                .statusCode(200);

        petApi.getPetById(petId)
                .then()
                .statusCode(404)
                .body("message", equalTo(PET_NOT_FOUND_MESSAGE));
    }

    @Test(description = "Create, delete, recreate — full lifecycle")
    @Severity(SeverityLevel.NORMAL)
    @Description("Create → delete → recreate — full lifecycle test")
    public void createDeleteRecreateLifecycle() {
        long petId = uniquePetId();
        String nameAfterRecreate = "LifecyclePetReborn";

        createPetForDeletion(petId, "LifecyclePet");

        petApi.deletePet(petId)
                .then()
                .statusCode(200);

        petApi.getPetById(petId)
                .then()
                .statusCode(404);

        createPetForDeletion(petId, nameAfterRecreate);

        petApi.getPetById(petId)
                .then()
                .statusCode(200)
                .body("name", equalTo(nameAfterRecreate));
    }

    // ── NEGATIVE SCENARIOS ──

    @Test(description = "Delete a non-existent pet")
    @Severity(SeverityLevel.NORMAL)
    @Description("DELETE /pet/{petId} — Expect 404 when deleting non-existent pet")
    public void deleteNonExistentPet() {
        long nonExistentId = 777777777L;
        petApi.deletePet(nonExistentId)
                .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    @Test(description = "Delete the same pet twice")
    @Severity(SeverityLevel.NORMAL)
    @Description("DELETE /pet/{petId} — Expect 404 on second deletion of the same pet")
    public void deleteSamePetTwice() {
        long petId = uniquePetId();
        String name = "DoubleDel";
        createPetForDeletion(petId, name);

        petApi.deletePet(petId)
                .then()
                .statusCode(200);

        petApi.deletePet(petId)
                .then()
                .statusCode(404)
                .body("message", equalTo(PET_NOT_FOUND_MESSAGE));
    }
}
