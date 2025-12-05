package it.unisa.c02.rently.rently_application.business.gestioneAnnuncio.controller;

import it.unisa.c02.rently.rently_application.business.gestioneAnnuncio.service.GestioneAnnuncioService;
import it.unisa.c02.rently.rently_application.business.gestioneAreaPersonale.service.GestioneAreaPersonaleService;
import it.unisa.c02.rently.rently_application.commons.services.regexService.RegexTester;
import it.unisa.c02.rently.rently_application.commons.services.responseService.ResponseService;
import it.unisa.c02.rently.rently_application.commons.services.storageService.FilesStorageService;
import it.unisa.c02.rently.rently_application.data.dto.AnnuncioDTO;
import it.unisa.c02.rently.rently_application.data.dto.ResponseDTO;
import it.unisa.c02.rently.rently_application.data.model.Annuncio;
import it.unisa.c02.rently.rently_application.data.model.Utente;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Controller che gestisce le operazioni relative agli annunci sulla piattaforma.
 * Fornisce endpoint per visualizzare annunci, aggiungere annunci, visualizzare annunci di un utente
 * e cancellare annunci dal sistema.
 * Questo controller gestisce anche l'upload di immagini associate agli annunci tramite il servizio FilesStorageService.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/annuncio")
@CrossOrigin(origins = {"*"}, methods = {
        RequestMethod.OPTIONS,
        RequestMethod.GET,
        RequestMethod.PUT,
        RequestMethod.DELETE,
        RequestMethod.POST
})
public class GestioneAnnuncioController {
    /**
     * Service di gestione dello storage dei file associati agli annunci.
     */
    private final FilesStorageService storageService;

    /**
     * Service per gestire le risposte delle richieste HTTP.
     */
    private final ResponseService responseService;

    /**
     * Service per effettuare le operazioni di persistenza legate agli annunci.
     */
    private final GestioneAnnuncioService gestioneAnnuncioService;

    /**
     * Service per la gestione dei dati personali degli utenti.
     */
    private final GestioneAreaPersonaleService gestioneAreaPersonaleService;

    /**
     * Oggetto HttpServletRequest per ottenere informazioni sulla richiesta HTTP.
     */
    private final HttpServletRequest httpServletRequest;

    /**
     * ResourceLoader per caricare risorse come file e URL.
     */
    private final ResourceLoader resourceLoader;

    /**
     * Percorso relativo per la directory di upload degli annunci.
     */
    private static final String uploadsPath = "annunci";

    /**
     * Percorso assoluto configurato per la cartella di upload delle immagini degli annunci.
     */
    @Value("${uploads.path}")
    private String uploadPath;

    /**
     * Restituisce le informazioni di un annuncio specifico in base all'identificativo.
     *
     * @param id Identificativo dell'annuncio da visualizzare.
     * @return ResponseEntity contenente le informazioni dell'annuncio o un messaggio di errore.
     */
    @GetMapping("/visualizza-annuncio")
    public ResponseEntity<String> getAnnuncio(@RequestParam final long id) {
        try {
            final Annuncio annuncio = gestioneAnnuncioService.getAnnuncio(id).orElse(null);
            final AnnuncioDTO item = new AnnuncioDTO().convertFromModel(annuncio);

            final String serverAddress = String.format(
                    "%s://%s:%d",
                    httpServletRequest.getScheme(),
                    httpServletRequest.getServerName(),
                    httpServletRequest.getServerPort()
            );

            item.setServerImage(annuncio, serverAddress);
            return responseService.Ok(item);

        } catch (Exception ex) {
            return responseService.InternalError();
        }
    }

    /**
     * Restituisce la lista di annunci associati a un utente specifico.
     *
     * @param id Identificativo dell'utente.
     * @return ResponseEntity contenente la lista di annunci dell'utente o un messaggio di errore.
     */
    @GetMapping("/visualizza-annunci-utente")
    public ResponseEntity<String> getAnnunciUtente(@RequestParam final long id) {
        try {
            final Utente u = gestioneAreaPersonaleService.getDatiPrivati(id);
            final List<Annuncio> annunci = gestioneAnnuncioService.findAllByUtente(u);
            final List<AnnuncioDTO> list = new ArrayList<>();

            final String serverAddress = String.format(
                    "%s://%s:%d",
                    httpServletRequest.getScheme(),
                    httpServletRequest.getServerName(),
                    httpServletRequest.getServerPort()
            );

            for (final Annuncio a : annunci) {
                final AnnuncioDTO item = new AnnuncioDTO().convertFromModel(a);
                item.setServerImage(a, serverAddress);
                list.add(item);
            }

            return responseService.Ok(list);
        } catch (Exception ex) {
            return responseService.InternalError();
        }
    }

    /**
     * Aggiunge un nuovo annuncio alla piattaforma.
     *
     * @param model AnnuncioDTO contenente le informazioni dell'annuncio.
     * @param image Immagine associata all'annuncio.
     * @return ResponseEntity contenente le informazioni dell'annuncio aggiunto o un messaggio di errore.
     */
    @PostMapping(value = "aggiungi-annuncio")
    public ResponseEntity<String> addAnnuncio(@ModelAttribute("model") final AnnuncioDTO model,
                                              @RequestParam("image") final MultipartFile image) {

        try {
            final ResponseDTO message = new ResponseDTO();
            message.message = "Dati inseriti non validi";

            final HashMap<String, String> tester = new HashMap<>();
            tester.put(model.getDescrizione(), "^[\\sa-zA-Z0-9.,:;'-èéòàùì]{1,1023}$");
            tester.put(model.getStrada(), "^[\\sa-zA-Z0-9.,:;'-èéòàùì]+$");
            tester.put(model.getCap(), "^[0-9]{5}$");
            tester.put(model.getNome(), "^[\\sa-zA-Z0-9.,'èéòàùì]{1,100}$");
            tester.put(model.getPrezzo().toString(), "^[0-9]{1,10}[.,][0-9]{2}$");

            final RegexTester regexTester = new RegexTester();
            if (!regexTester.toTest(tester)) {
                return responseService.InternalError(message);
            }

            final Annuncio item = new Annuncio();
            item.setNome(model.getNome());
            item.setStrada(model.getStrada());
            item.setCitta(model.getCitta());
            item.setCap(model.getCap());
            item.setDescrizione(model.getDescrizione());
            item.setPrezzo(model.getPrezzo());
            item.setCategoria(Annuncio.EnumCategoria.valueOf(model.getCategoria().toUpperCase()));
            item.setCondizione(Annuncio.EnumCondizione.valueOf(model.getCondizione().toUpperCase()));
            item.setDataFine(Date.valueOf(model.getDataFine()));

            final Utente user = gestioneAreaPersonaleService.getDatiPrivati(model.getIdUtente());
            if (user != null)
                item.setUtente(user);

            final Annuncio newItem = gestioneAnnuncioService.addAnnuncio(item);

            final String basePath = uploadPath + "annunci/" + newItem.getId() + "/";
            storageService.init(basePath);

            String fileName = storageService.generateRandomFileName();
            final String extension = image.getOriginalFilename()
                    .substring(image.getOriginalFilename().lastIndexOf('.') + 1);
            fileName = fileName + "." + extension;

            storageService.save(image, fileName);
            newItem.setImmagine(fileName);

            gestioneAnnuncioService.updateAnnuncio(newItem);

            final AnnuncioDTO annuncioDto = new AnnuncioDTO().convertFromModel(newItem);
            return responseService.Ok(annuncioDto);

        } catch (Exception ex) {
            return responseService.InternalError();
        }
    }

    /**
     * Modifica un annuncio esistente e, se presente, aggiorna anche l'immagine associata.
     *
     * @param model AnnuncioDTO con i nuovi dati dell'annuncio.
     * @param image (Opzionale) nuova immagine dell'annuncio.
     * @return ResponseEntity contenente l'annuncio aggiornato o errore.
     */
    @PostMapping(value = "modifica-annuncio")
    public ResponseEntity<String> modifyAnnuncio(@ModelAttribute("model") final AnnuncioDTO model,
                                                 @RequestParam(value = "image", required = false) final MultipartFile image) {

        try {
            final Annuncio item = gestioneAnnuncioService.getAnnuncio(model.getId()).orElse(null);
            if (item == null)
                return responseService.InternalError();

            item.setNome(model.getNome());
            item.setStrada(model.getStrada());
            item.setCitta(model.getCitta());
            item.setCap(model.getCap());
            item.setDescrizione(model.getDescrizione());
            item.setPrezzo(model.getPrezzo());
            item.setCategoria(Annuncio.EnumCategoria.valueOf(model.getCategoria().toUpperCase()));
            item.setCondizione(Annuncio.EnumCondizione.valueOf(model.getCondizione().toUpperCase()));
            item.setDataFine(Date.valueOf(model.getDataFine()));

            final Utente user = gestioneAreaPersonaleService.getDatiPrivati(model.getIdUtente());
            if (user != null)
                item.setUtente(user);

            final Annuncio newItem = gestioneAnnuncioService.updateAnnuncio(item);

            if (image != null) {
                final String basePath = uploadPath + "annunci/" + newItem.getId() + "/";
                storageService.init(basePath);

                String fileName = storageService.generateRandomFileName();
                final String extension = image.getOriginalFilename()
                        .substring(image.getOriginalFilename().lastIndexOf('.') + 1);
                fileName = fileName + "." + extension;

                storageService.deleteAll();
                storageService.save(image, fileName);

                newItem.setImmagine(fileName);
                gestioneAnnuncioService.updateAnnuncio(newItem);
            }

            final AnnuncioDTO annuncioDto = new AnnuncioDTO().convertFromModel(newItem);
            return responseService.Ok(annuncioDto);

        } catch (Exception ex) {
            return responseService.InternalError();
        }
    }

    /**
     * Cancella un annuncio dalla piattaforma in base all'identificativo.
     *
     * @param id Identificativo dell'annuncio da cancellare.
     * @return ResponseEntity con esito positivo o messaggio di errore.
     */
    @GetMapping("/delete-annuncio")
    public ResponseEntity<String> deleteAnnuncio(@RequestParam final long id) {
        try {
            gestioneAnnuncioService.deleteAnnuncio(id);
            return responseService.Ok();
        } catch (Exception ex) {
            return responseService.InternalError();
        }
    }
}

