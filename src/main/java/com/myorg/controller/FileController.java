package com.myorg.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.entity.FileDetail;
import com.myorg.service.FileService;

/**
 * REST API File controller to handle save and search of file from/to database/disk.
 * 
 * @author Fnu Utpal
 *
 */
@RestController
@RequestMapping("/files")
public class FileController {
    public static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private static String FILE_UPLOAD_SUCCESS_MESSAGE = "You successfully uploaded ";

    @Autowired
    FileService fileService;

    /**
     * This method searches by file id stored in database.
     * 
     * @param id
     *            File Identifier.
     * 
     * @return FileDetail object containing file meta data and file content.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<FileDetail> findFileById(@PathVariable("id") long id) {
        FileDetail fileDetail = fileService.getFileDetail(id);
        return ResponseEntity.ok().body(fileDetail);
    }

    /**
     * Saves file meta data and file content into database.
     * 
     * @param fileMetaData
     *            File meta data object in JSON format.
     * @param fileContent
     *            File content.
     * 
     * @return File Detail.
     * 
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/save-to-db", method = RequestMethod.POST)
    public ResponseEntity<FileDetail> saveFileToDB(@RequestPart("fileMetaData") String fileMetaData,
            @RequestPart("fileContent") MultipartFile fileContent)
            throws JsonParseException, JsonMappingException, IOException {
        FileDetail fileDetail = new ObjectMapper().readValue(fileMetaData, FileDetail.class);
        fileDetail.setContent(fileContent.getBytes());

        fileService.saveFile(fileDetail);
        return ResponseEntity.ok().body(fileDetail);
    }

    /**
     * Saves file content over local file system.
     * 
     * @param file
     *            MultipartFile
     * 
     * @return String message
     */
    @RequestMapping(value = "/save-to-disk", method = RequestMethod.POST)
    public ResponseEntity<String> saveFileToDisk(@RequestPart("file") MultipartFile file) {
        fileService.saveFileToDisk(file);
        return ResponseEntity.ok().body(FILE_UPLOAD_SUCCESS_MESSAGE + file.getOriginalFilename());
    }

    /**
     * Searches by file name stored over local file system.
     * 
     * @param filename
     * 
     * @return resource
     */
    @GetMapping
    public ResponseEntity<Resource> findFileByName(@RequestParam(value = "fileName", required = true) String fileName) {
        Resource file = fileService.loadAsResource(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

}
