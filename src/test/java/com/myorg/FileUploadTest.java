package com.myorg;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.myorg.exception.StorageFileNotFoundException;
import com.myorg.service.FileService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class FileUploadTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FileService fileService;

    @SuppressWarnings("deprecation")
    @Test
    public void whenFileIsUploaded_thenFileShouldBeUploadedToDisk() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain",
                "Spring Framework".getBytes());
        this.mvc.perform(fileUpload("/files/save-to-disk").file(multipartFile)).andExpect(status().isOk());

        then(this.fileService).should().saveFileToDisk(multipartFile);
    }

    @Test
    public void whenFileIsSearchedByName_thenFileShouldBeReturned() throws Exception {
        ClassPathResource resource = new ClassPathResource("test.txt", getClass());
        given(this.fileService.loadAsResource("test.txt")).willReturn(resource);

        this.mvc.perform(get("/files?fileName=test.txt")).andExpect(status().isOk());
    }

    @Test
    public void whenNonExistingFileIsSearchedByName_thenFileShouldNotBeReturned() throws Exception {
        given(this.fileService.loadAsResource("testnew.txt")).willThrow(StorageFileNotFoundException.class);

        this.mvc.perform(get("/files?fileName=testnew.txt")).andExpect(status().isNotFound());
    }

    @Test
    public void whenFileIsSearchedByName_thenFileShouldBeDownloaded() throws Exception {
        ClassPathResource resource = new ClassPathResource("test.txt", getClass());
        given(this.fileService.loadAsResource("test.txt")).willReturn(resource);

        ResponseEntity<String> response = this.restTemplate.getForEntity("/files?fileName=test.txt", String.class,
                "test.txt");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"test.txt\"");
        assertThat(response.getBody()).isEqualTo("Spring Framework");
    }

}
