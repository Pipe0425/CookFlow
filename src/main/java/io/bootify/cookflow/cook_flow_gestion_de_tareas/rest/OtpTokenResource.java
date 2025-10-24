package io.bootify.cookflow.cook_flow_gestion_de_tareas.rest;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.OtpTokenDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.service.OtpTokenService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/otpTokens", produces = MediaType.APPLICATION_JSON_VALUE)
public class OtpTokenResource {

    private final OtpTokenService otpTokenService;

    public OtpTokenResource(final OtpTokenService otpTokenService) {
        this.otpTokenService = otpTokenService;
    }

    @GetMapping
    public ResponseEntity<List<OtpTokenDTO>> getAllOtpTokens() {
        return ResponseEntity.ok(otpTokenService.findAll());
    }

    @GetMapping("/{idOtpToken}")
    public ResponseEntity<OtpTokenDTO> getOtpToken(
            @PathVariable(name = "idOtpToken") final Long idOtpToken) {
        return ResponseEntity.ok(otpTokenService.get(idOtpToken));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createOtpToken(@RequestBody @Valid final OtpTokenDTO otpTokenDTO) {
        final Long createdIdOtpToken = otpTokenService.create(otpTokenDTO);
        return new ResponseEntity<>(createdIdOtpToken, HttpStatus.CREATED);
    }

    @PutMapping("/{idOtpToken}")
    public ResponseEntity<Long> updateOtpToken(
            @PathVariable(name = "idOtpToken") final Long idOtpToken,
            @RequestBody @Valid final OtpTokenDTO otpTokenDTO) {
        otpTokenService.update(idOtpToken, otpTokenDTO);
        return ResponseEntity.ok(idOtpToken);
    }

    @DeleteMapping("/{idOtpToken}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteOtpToken(
            @PathVariable(name = "idOtpToken") final Long idOtpToken) {
        otpTokenService.delete(idOtpToken);
        return ResponseEntity.noContent().build();
    }

}
