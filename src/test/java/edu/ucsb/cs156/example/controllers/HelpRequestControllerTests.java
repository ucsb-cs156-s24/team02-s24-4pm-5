package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBDiningCommonsController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsControllerTests extends ControllerTestCase {

        @MockBean
        UCSBDiningCommonsRepository ucsbDiningCommonsRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/ucsbdiningcommons/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommons/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommons/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbDiningCommonsRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommons?code=munger-hall"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbDiningCommonsRepository, times(1)).findById(eq("munger-hall"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBDiningCommons with id munger-hall not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbdiningcommons() throws Exception {

                // arrange

                UCSBDiningCommons carrillo = UCSBDiningCommons.builder()
                                .name("Carrillo")
                                .code("carrillo")
                                .hasSackMeal(false)
                                .hasTakeOutMeal(false)
                                .hasDiningCam(true)
                                .latitude(34.409953)
                                .longitude(-119.85277)
                                .build();

                UCSBDiningCommons dlg = UCSBDiningCommons.builder()
                                .name("De La Guerra")
                                .code("de-la-guerra")
                                .hasSackMeal(false)
                                .hasTakeOutMeal(false)
                                .hasDiningCam(true)
                                .latitude(34.409811)
                                .longitude(-119.845026)
                                .build();

                ArrayList<UCSBDiningCommons> expectedCommons = new ArrayList<>();
                expectedCommons.addAll(Arrays.asList(carrillo, dlg));

                when(ucsbDiningCommonsRepository.findAll()).thenReturn(expectedCommons);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommons/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedCommons);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/ucsbdiningcommons...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommons/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommons/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_commons() throws Exception {
                // arrange

                UCSBDiningCommons ortega = UCSBDiningCommons.builder()
                                .name("Ortega")
                                .code("ortega")
                                .hasSackMeal(true)
                                .hasTakeOutMeal(true)
                                .hasDiningCam(true)
                                .latitude(34.410987)
                                .longitude(-119.84709)
                                .build();

                when(ucsbDiningCommonsRepository.save(eq(ortega))).thenReturn(ortega);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsbdiningcommons/post?name=Ortega&code=ortega&hasSackMeal=true&hasTakeOutMeal=true&hasDiningCam=true&latitude=34.410987&longitude=-119.84709")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsRepository, times(1)).save(ortega);
                String expectedJson = mapper.writeValueAsString(ortega);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

}
