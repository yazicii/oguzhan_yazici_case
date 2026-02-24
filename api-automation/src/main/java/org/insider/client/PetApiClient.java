package org.insider.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.insider.models.Pet;

import static io.restassured.RestAssured.given;

/**
 * REST client for Petstore /pet endpoints.
 * Wraps all HTTP calls; tests interact with the API through this client.
 */
public class PetApiClient {

    private static final String PET_PATH = "/pet";
    private static final String PET_BY_ID_PATH = "/pet/{petId}";
    private static final String PET_FIND_BY_STATUS_PATH = "/pet/findByStatus";

    private final RequestSpecification spec;

    public PetApiClient(RequestSpecification spec) {
        this.spec = spec;
    }

    // ── CREATE ──

    public Response createPet(Pet pet) {
        return given()
                .spec(spec)
                .body(pet)
                .when()
                .post(PET_PATH);
    }

    public Response createPetWithRawBody(String rawJson) {
        return given()
                .spec(spec)
                .body(rawJson)
                .when()
                .post(PET_PATH);
    }

    // ── READ ──

    public Response getPetById(long petId) {
        return given()
                .spec(spec)
                .pathParam("petId", petId)
                .when()
                .get(PET_BY_ID_PATH);
    }

    public Response getPetByIdRaw(String petId) {
        return given()
                .spec(spec)
                .pathParam("petId", petId)
                .when()
                .get(PET_BY_ID_PATH);
    }

    public Response findPetsByStatus(String status) {
        return given()
                .spec(spec)
                .queryParam("status", status)
                .when()
                .get(PET_FIND_BY_STATUS_PATH);
    }

    // ── UPDATE ──

    public Response updatePet(Pet pet) {
        return given()
                .spec(spec)
                .body(pet)
                .when()
                .put(PET_PATH);
    }

    public Response updatePetWithRawBody(String rawJson) {
        return given()
                .spec(spec)
                .body(rawJson)
                .when()
                .put(PET_PATH);
    }

    // ── DELETE ──

    public Response deletePet(long petId) {
        return given()
                .spec(spec)
                .pathParam("petId", petId)
                .when()
                .delete(PET_BY_ID_PATH);
    }
}
