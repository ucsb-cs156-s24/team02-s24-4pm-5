package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;

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

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

    @MockBean
    MenuItemReviewRepository menuitemReviewRepository;

    @MockBean
    UserRepository userRepository;

    // Tests for GET /api/MenuItemReview/all
        
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/MenuItemReview/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/MenuItemReview/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_menuitemreviews() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-01-04T00:00:00");

                MenuItemReview menuitemReview1 = MenuItemReview.builder()
                                .itemId(7L)      
                                .reviewerEmail("cgaucho@ucsb.edu")
                                .stars(5)
                                .dateReviewed(ldt1)
                                .comments("I love the apple pie")
                                .build();

                MenuItemReview menuitemReview2 = MenuItemReview.builder()
                                .itemId(7L)      
                                .reviewerEmail("ldelplaya@ucsb.edu")
                                .stars(0)
                                .dateReviewed(ldt2)
                                .comments("I hate the apple pie")
                                .build();

                ArrayList<MenuItemReview> expectedReviews = new ArrayList<>();
                expectedReviews.addAll(Arrays.asList(menuitemReview1, menuitemReview2));

                when(menuitemReviewRepository.findAll()).thenReturn(expectedReviews);

                // act
                MvcResult response = mockMvc.perform(get("/api/MenuItemReview/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(menuitemReviewRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedReviews);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/MenuItemReview/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/MenuItemReview/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/MenuItemReview/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_menuitemreview() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                MenuItemReview menuitemReview1 = MenuItemReview.builder()
                                .itemId(7L)      
                                .reviewerEmail("cgaucho@ucsb.edu")
                                .stars(5)       
                                .dateReviewed(ldt1)
                                .comments("I love the apple pie")
                                .build();


                when(menuitemReviewRepository.save(eq(menuitemReview1))).thenReturn(menuitemReview1);

                // act

                MvcResult response = mockMvc.perform(
                                post("/api/MenuItemReview/post?itemId=7&reviewerEmail=cgaucho@ucsb.edu&stars=5&dateReviewed=2022-01-03T00:00:00&comments=I love the apple pie")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(menuitemReviewRepository, times(1)).save(menuitemReview1);
                String expectedJson = mapper.writeValueAsString(menuitemReview1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }


        // Tests for GET /api/MenuItemReview?id=...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/MenuItemReview?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                MenuItemReview menuitemReview = MenuItemReview.builder()
                                .itemId(7L)      
                                .reviewerEmail("cgaucho@ucsb.edu")
                                .stars(5)       
                                .dateReviewed(ldt1)
                                .comments("I love the apple pie")
                                .build();

                when(menuitemReviewRepository.findById(eq(7L))).thenReturn(Optional.of(menuitemReview));

                // act
                MvcResult response = mockMvc.perform(get("/api/MenuItemReview?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(menuitemReviewRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(menuitemReview);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(menuitemReviewRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/MenuItemReview?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(menuitemReviewRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("MenuItemReview with id 7 not found", json.get("message"));
        }


        // Tests for DELETE /api/MenuItemReview?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_menuitemreview() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                MenuItemReview menuitemReview = MenuItemReview.builder()
                                .itemId(7L)      
                                .reviewerEmail("cgaucho@ucsb.edu")
                                .stars(5)       
                                .dateReviewed(ldt1)
                                .comments("I love the apple pie")
                                .build();

                when(menuitemReviewRepository.findById(eq(17L))).thenReturn(Optional.of(menuitemReview));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/MenuItemReview?id=17")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(menuitemReviewRepository, times(1)).findById(17L);
                verify(menuitemReviewRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("MenuItemReview with id 17 deleted", json.get("message"));
        }
        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_menuitemreview_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(menuitemReviewRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/MenuItemReview?id=17")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(menuitemReviewRepository, times(1)).findById(17L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("MenuItemReview with id 17 not found", json.get("message"));
        }

        // Tests for PUT /api/MenuItemReview?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_menuitemreview() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2024-04-20T00:00:00");
                LocalDateTime ldt3 = LocalDateTime.parse("2022-01-05T00:00:00");
                LocalDateTime ldt4 = LocalDateTime.parse("2023-01-06T00:00:00");

                MenuItemReview menuitemReviewOrig = MenuItemReview.builder()
                                .itemId(7L)      
                                .reviewerEmail("cgaucho@ucsb.edu")
                                .stars(5)       
                                .dateReviewed(ldt1)
                                .comments("I love the apple pie")
                                .build();

                MenuItemReview menuitemReviewEdited = MenuItemReview.builder()
                                .itemId(9L)      
                                .reviewerEmail("stevenle@ucsb.edu")
                                .stars(3)       
                                .dateReviewed(ldt2)
                                .comments("This cookie is alright.")
                                .build();

                String requestBody = mapper.writeValueAsString(menuitemReviewEdited);

                when(menuitemReviewRepository.findById(eq(67L))).thenReturn(Optional.of(menuitemReviewOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/MenuItemReview?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(menuitemReviewRepository, times(1)).findById(67L);
                verify(menuitemReviewRepository, times(1)).save(menuitemReviewEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_menuitemreview_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");


                MenuItemReview editedMenuItemReview = MenuItemReview.builder()
                                .itemId(7L)      
                                .reviewerEmail("cgaucho@ucsb.edu")
                                .stars(5)       
                                .dateReviewed(ldt1)
                                .comments("I love the apple pie")
                                .build();

                String requestBody = mapper.writeValueAsString(editedMenuItemReview);

                when(menuitemReviewRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/MenuItemReview?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(menuitemReviewRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("MenuItemReview with id 67 not found", json.get("message"));

        }


}