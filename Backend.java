
// pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.example</groupId>
    <artifactId>calendar-booking-api</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <properties>
        <java.version>17</java.version>
        <google-api-client.version>2.2.0</google-api-client.version>
        <google-calendar.version>v3-rev20231016-2.0.0</google-calendar.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.api-client</groupId>
            <artifactId>google-api-client</artifactId>
            <version>${google-api-client.version}</version>
        </dependency>
        <dependency>
            <groupId>com.google.apis</groupId>
            <artifactId>google-api-services-calendar</artifactId>
            <version>${google-calendar.version}</version>
        </dependency>
        <dependency>
            <groupId>com.google.auth</groupId>
            <artifactId>google-auth-library-oauth2-http</artifactId>
            <version>1.19.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>

// ==============================================================================
// Application Configuration
// ==============================================================================

// src/main/java/com/example/calendarbooking/CalendarBookingApplication.java
package com.example.calendarbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CalendarBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CalendarBookingApplication.class, args);
    }
}

// ==============================================================================
// Configuration Properties
// ==============================================================================

// src/main/java/com/example/calendarbooking/config/GoogleCalendarProperties.java
package com.example.calendarbooking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "google.calendar")
public class GoogleCalendarProperties {
    private String calendarId;
    private String serviceAccountKeyPath;
    private String timeZone = "Asia/Kolkata";
    private int slotDurationMinutes = 30;
    private String applicationName = "Calendar Booking API";

    // Getters and setters
    public String getCalendarId() { return calendarId; }
    public void setCalendarId(String calendarId) { this.calendarId = calendarId; }
    
    public String getServiceAccountKeyPath() { return serviceAccountKeyPath; }
    public void setServiceAccountKeyPath(String serviceAccountKeyPath) { 
        this.serviceAccountKeyPath = serviceAccountKeyPath; 
    }
    
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
    
    public int getSlotDurationMinutes() { return slotDurationMinutes; }
    public void setSlotDurationMinutes(int slotDurationMinutes) { 
        this.slotDurationMinutes = slotDurationMinutes; 
    }
    
    public String getApplicationName() { return applicationName; }
    public void setApplicationName(String applicationName) { 
        this.applicationName = applicationName; 
    }
}

// ==============================================================================
// Google Calendar Configuration
// ==============================================================================

// src/main/java/com/example/calendarbooking/config/GoogleCalendarConfig.java
package com.example.calendarbooking.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleCalendarConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarConfig.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    @Autowired
    private GoogleCalendarProperties properties;
    
    @Bean
    public Calendar googleCalendar() throws IOException, GeneralSecurityException {
        logger.info("Initializing Google Calendar service");
        
        GoogleCredentials credentials = ServiceAccountCredentials
            .fromStream(new FileInputStream(properties.getServiceAccountKeyPath()))
            .createScoped(Collections.singleton(CalendarScopes.CALENDAR));
        
        return new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JSON_FACTORY,
            new HttpCredentialsAdapter(credentials))
            .setApplicationName(properties.getApplicationName())
            .build();
    }
}

// ==============================================================================
// DTOs (Data Transfer Objects)
// ==============================================================================

// src/main/java/com/example/calendarbooking/dto/AvailabilityRequest.java
package com.example.calendarbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AvailabilityRequest {
    
    @NotBlank(message = "Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in YYYY-MM-DD format")
    private String date;
    
    @NotBlank(message = "Time is required")
    @Pattern(regexp = "\\d{2}:\\d{2}", message = "Time must be in HH:MM format")
    private String time;
    
    // Constructors
    public AvailabilityRequest() {}
    
    public AvailabilityRequest(String date, String time) {
        this.date = date;
        this.time = time;
    }
    
    // Getters and setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}

// src/main/java/com/example/calendarbooking/dto/AvailabilityResponse.java
package com.example.calendarbooking.dto;

import java.util.List;

public class AvailabilityResponse {
    private String confirmation;
    private List<String> alternateTimeSlots;
    
    // Constructors
    public AvailabilityResponse() {}
    
    public AvailabilityResponse(String confirmation, List<String> alternateTimeSlots) {
        this.confirmation = confirmation;
        this.alternateTimeSlots = alternateTimeSlots;
    }
    
    // Getters and setters
    public String getConfirmation() { return confirmation; }
    public void setConfirmation(String confirmation) { this.confirmation = confirmation; }
    
    public List<String> getAlternateTimeSlots() { return alternateTimeSlots; }
    public void setAlternateTimeSlots(List<String> alternateTimeSlots) { 
        this.alternateTimeSlots = alternateTimeSlots; 
    }
}

// src/main/java/com/example/calendarbooking/dto/BookingRequest.java
package com.example.calendarbooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BookingRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in YYYY-MM-DD format")
    private String date;
    
    @NotBlank(message = "Time is required")
    @Pattern(regexp = "\\d{2}:\\d{2}", message = "Time must be in HH:MM format")
    private String time;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\+\\d{10,15}", message = "Phone number must start with + and contain 10-15 digits")
    private String phoneNum;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Purpose of visit is required")
    @Size(min = 5, max = 200, message = "Purpose must be between 5 and 200 characters")
    private String purposeOfVisit;
    
    // Constructors
    public BookingRequest() {}
    
    public BookingRequest(String name, String date, String time, String phoneNum, 
                         String email, String purposeOfVisit) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.phoneNum = phoneNum;
        this.email = email;
        this.purposeOfVisit = purposeOfVisit;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    
    public String getPhoneNum() { return phoneNum; }
    public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPurposeOfVisit() { return purposeOfVisit; }
    public void setPurposeOfVisit(String purposeOfVisit) { this.purposeOfVisit = purposeOfVisit; }
}

// src/main/java/com/example/calendarbooking/dto/BookingResponse.java
package com.example.calendarbooking.dto;

public class BookingResponse {
    private String status;
    private String message;
    
    // Constructors
    public BookingResponse() {}
    
    public BookingResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
    
    // Getters and setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

// ==============================================================================
// Exception Classes
// ==============================================================================

// src/main/java/com/example/calendarbooking/exception/CalendarServiceException.java
package com.example.calendarbooking.exception;

public class CalendarServiceException extends RuntimeException {
    public CalendarServiceException(String message) {
        super(message);
    }
    
    public CalendarServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

// src/main/java/com/example/calendarbooking/exception/TimeSlotUnavailableException.java
package com.example.calendarbooking.exception;

public class TimeSlotUnavailableException extends RuntimeException {
    public TimeSlotUnavailableException(String message) {
        super(message);
    }
}

// ==============================================================================
// Service Layer
// ==============================================================================

// src/main/java/com/example/calendarbooking/service/CalendarService.java
package com.example.calendarbooking.service;

import com.example.calendarbooking.config.GoogleCalendarProperties;
import com.example.calendarbooking.dto.BookingRequest;
import com.example.calendarbooking.exception.CalendarServiceException;
import com.example.calendarbooking.exception.TimeSlotUnavailableException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalendarService {
    
    private static final Logger logger = LoggerFactory.getLogger(CalendarService.class);
    
    @Autowired
    private Calendar calendar;
    
    @Autowired
    private GoogleCalendarProperties properties;
    
    public boolean isTimeSlotAvailable(String date, String time) {
        try {
            ZonedDateTime requestedDateTime = parseDateTime(date, time);
            ZonedDateTime endDateTime = requestedDateTime.plusMinutes(properties.getSlotDurationMinutes());
            
            logger.info("Checking availability for {} to {}", requestedDateTime, endDateTime);
            
            Events events = calendar.events().list(properties.getCalendarId())
                .setTimeMin(new DateTime(requestedDateTime.toInstant().toEpochMilli()))
                .setTimeMax(new DateTime(endDateTime.toInstant().toEpochMilli()))
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();
            
            List<Event> items = events.getItems();
            boolean isAvailable = items.isEmpty();
            
            logger.info("Time slot availability: {}, Found {} conflicting events", 
                       isAvailable, items.size());
            
            return isAvailable;
            
        } catch (IOException e) {
            logger.error("Error checking calendar availability", e);
            throw new CalendarServiceException("Failed to check calendar availability", e);
        }
    }
    
    public List<String> findAlternateTimeSlots(String date) {
        List<String> alternateSlots = new ArrayList<>();
        
        try {
            LocalDate localDate = LocalDate.parse(date);
            ZoneId zoneId = ZoneId.of(properties.getTimeZone());
            
            // Check slots from 9 AM to 6 PM
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime = LocalTime.of(18, 0);
            
            LocalTime currentTime = startTime;
            while (currentTime.isBefore(endTime)) {
                ZonedDateTime slotDateTime = ZonedDateTime.of(localDate, currentTime, zoneId);
                
                if (isTimeSlotAvailable(date, currentTime.format(DateTimeFormatter.ofPattern("HH:mm")))) {
                    alternateSlots.add(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
                
                currentTime = currentTime.plusMinutes(properties.getSlotDurationMinutes());
                
                // Limit to 5 alternate slots
                if (alternateSlots.size() >= 5) {
                    break;
                }
            }
            
        } catch (Exception e) {
            logger.error("Error finding alternate time slots for date: {}", date, e);
        }
        
        return alternateSlots;
    }
    
    public void bookAppointment(BookingRequest request) {
        try {
            // Double-check availability to prevent race conditions
            if (!isTimeSlotAvailable(request.getDate(), request.getTime())) {
                throw new TimeSlotUnavailableException("Time slot is no longer available");
            }
            
            ZonedDateTime startDateTime = parseDateTime(request.getDate(), request.getTime());
            ZonedDateTime endDateTime = startDateTime.plusMinutes(properties.getSlotDurationMinutes());
            
            Event event = new Event()
                .setSummary("Appointment: " + request.getName())
                .setDescription(buildEventDescription(request));
            
            EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(startDateTime.toInstant().toEpochMilli()))
                .setTimeZone(properties.getTimeZone());
            event.setStart(start);
            
            EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(endDateTime.toInstant().toEpochMilli()))
                .setTimeZone(properties.getTimeZone());
            event.setEnd(end);
            
            event = calendar.events().insert(properties.getCalendarId(), event).execute();
            
            logger.info("Successfully created calendar event with ID: {}", event.getId());
            
        } catch (TimeSlotUnavailableException e) {
            throw e;
        } catch (IOException e) {
            logger.error("Error creating calendar event", e);
            throw new CalendarServiceException("Failed to create calendar event", e);
        }
    }
    
    private ZonedDateTime parseDateTime(String date, String time) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            LocalTime localTime = LocalTime.parse(time);
            ZoneId zoneId = ZoneId.of(properties.getTimeZone());
            
            return ZonedDateTime.of(localDate, localTime, zoneId);
        } catch (DateTimeParseException e) {
            logger.error("Error parsing date/time: {} {}", date, time, e);
            throw new IllegalArgumentException("Invalid date or time format", e);
        }
    }
    
    private String buildEventDescription(BookingRequest request) {
        return String.format(
            "Appointment Details:\n" +
            "Name: %s\n" +
            "Phone: %s\n" +
            "Email: %s\n" +
            "Purpose: %s\n" +
            "Date: %s\n" +
            "Time: %s",
            request.getName(),
            request.getPhoneNum(),
            request.getEmail(),
            request.getPurposeOfVisit(),
            request.getDate(),
            request.getTime()
        );
    }
}

// ==============================================================================
// Controller Layer
// ==============================================================================

// src/main/java/com/example/calendarbooking/controller/BookingController.java
package com.example.calendarbooking.controller;

import com.example.calendarbooking.dto.*;
import com.example.calendarbooking.exception.CalendarServiceException;
import com.example.calendarbooking.exception.TimeSlotUnavailableException;
import com.example.calendarbooking.service.CalendarService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class BookingController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    
    @Autowired
    private CalendarService calendarService;
    
    @PostMapping("/check-availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @Valid @RequestBody AvailabilityRequest request,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors in availability request: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest()
                .body(new AvailabilityResponse("error", Collections.emptyList()));
        }
        
        try {
            logger.info("Checking availability for date: {}, time: {}", 
                       request.getDate(), request.getTime());
            
            boolean isAvailable = calendarService.isTimeSlotAvailable(
                request.getDate(), request.getTime());
            
            if (isAvailable) {
                return ResponseEntity.ok(new AvailabilityResponse("yes", Collections.emptyList()));
            } else {
                List<String> alternateSlots = calendarService.findAlternateTimeSlots(request.getDate());
                return ResponseEntity.ok(new AvailabilityResponse("no", alternateSlots));
            }
            
        } catch (CalendarServiceException e) {
            logger.error("Calendar service error during availability check", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AvailabilityResponse("error", Collections.emptyList()));
        } catch (Exception e) {
            logger.error("Unexpected error during availability check", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AvailabilityResponse("error", Collections.emptyList()));
        }
    }
    
    @PostMapping("/book")
    public ResponseEntity<BookingResponse> bookAppointment(
            @Valid @RequestBody BookingRequest request,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors in booking request: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest()
                .body(new BookingResponse("Failed", "Invalid request data"));
        }
        
        try {
            logger.info("Processing booking request for {} on {} at {}", 
                       request.getName(), request.getDate(), request.getTime());
            
            calendarService.bookAppointment(request);
            
            String message = String.format(
                "Your appointment has been successfully scheduled for %s at %s",
                request.getDate(), request.getTime());
            
            return ResponseEntity.ok(new BookingResponse("Booked", message));
            
        } catch (TimeSlotUnavailableException e) {
            logger.warn("Time slot unavailable: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new BookingResponse("Failed", 
                    "The selected time slot is no longer available. Please try another time."));
        } catch (CalendarServiceException e) {
            logger.error("Calendar service error during booking", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BookingResponse("Failed", 
                    "Unable to process your booking at this time. Please try again later."));
        } catch (Exception e) {
            logger.error("Unexpected error during booking", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BookingResponse("Failed", 
                    "An unexpected error occurred. Please try again later."));
        }
    }
}

// ==============================================================================
// Global Exception Handler
// ==============================================================================

// src/main/java/com/example/calendarbooking/exception/GlobalExceptionHandler.java
package com.example.calendarbooking.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        response.put("status", "error");
        response.put("message", "Validation failed");
        response.put("errors", errors);
        
        logger.warn("Validation error: {}", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(CalendarServiceException.class)
    public ResponseEntity<Map<String, String>> handleCalendarServiceException(
            CalendarServiceException ex) {
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Calendar service is temporarily unavailable");
        
        logger.error("Calendar service exception", ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "An unexpected error occurred");
        
        logger.error("Unexpected exception", ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

// ==============================================================================
// Configuration Files
// ==============================================================================

// src/main/resources/application.properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# Google Calendar Configuration
google.calendar.calendar-id=your-calendar-id@group.calendar.google.com
google.calendar.service-account-key-path=/path/to/service-account-key.json
google.calendar.time-zone=Asia/Kolkata
google.calendar.slot-duration-minutes=30
google.calendar.application-name=Calendar Booking API

# Logging Configuration
logging.level.com.example.calendarbooking=INFO
logging.level.com.google.api=WARN
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Actuator Configuration
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized

# ==============================================================================
// Unit Tests
// ==============================================================================

// src/test/java/com/example/calendarbooking/service/CalendarServiceTest.java
package com.example.calendarbooking.service;

import com.example.calendarbooking.config.GoogleCalendarProperties;
import com.example.calendarbooking.dto.BookingRequest;
import com.example.calendarbooking.exception.CalendarServiceException;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {
    
    @Mock
    private Calendar calendar;
    
    @Mock
    private GoogleCalendarProperties properties;
    
    @Mock
    private Calendar.Events events;
    
    @Mock
    private Calendar.Events.List eventsList;
    
    @InjectMocks
    private CalendarService calendarService;
    
    @BeforeEach
    void setUp() {
        when(properties.getCalendarId()).thenReturn("test-calendar@example.com");
        when(properties.getTimeZone()).thenReturn("Asia/Kolkata");
        when(properties.getSlotDurationMinutes()).thenReturn(30);
    }
    
    @Test
    void testIsTimeSlotAvailable_WhenSlotIsFree() throws IOException {
        // Arrange
        Events emptyEvents = new Events();
        emptyEvents.setItems(Collections.emptyList());
        
        when(calendar.events()).thenReturn(events);
        when(events.list(anyString())).thenReturn(eventsList);
        when(eventsList.setTimeMin(any())).thenReturn(eventsList);
        when(eventsList.setTimeMax(any())).thenReturn(eventsList);
        when(eventsList.setSingleEvents(true)).thenReturn(eventsList);
        when(eventsList.setOrderBy(anyString())).thenReturn(eventsList);
        when(eventsList.execute()).thenReturn(emptyEvents);
        
        // Act
        boolean result = calendarService.isTimeSlotAvailable("2025-06-14", "15:30");
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void testIsTimeSlotAvailable_WhenSlotIsOccupied() throws IOException {
        // Arrange
        Events occupiedEvents = new Events();
        occupiedEvents.setItems(List.of(new com.google.api.services.calendar.model.Event()));
        
        when(calendar.events()).thenReturn(events);
        when(events.list(anyString())).thenReturn(eventsList);
        when(eventsList.setTimeMin(any())).thenReturn(eventsList);
        when(eventsList.setTimeMax(any())).thenReturn(eventsList);
        when(eventsList.setSingleEvents(true)).thenReturn(eventsList);
        when(eventsList.setOrderBy(anyString())).thenReturn(eventsList);
        when(eventsList.execute()).thenReturn(occupiedEvents);
        
        // Act
        boolean result = calendarService.isTimeSlotAvailable("2025-06-14", "15:30");
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void testIsTimeSlotAvailable_WhenIOException() throws IOException {
        // Arrange
        when(calendar.events()).thenReturn(events);
        when(events.list(anyString())).thenReturn(eventsList);
        when(eventsList.setTimeMin(any())).thenReturn(eventsList);
        when(eventsList.setTimeMax(any())).thenReturn(eventsList);
        when(eventsList.setSingleEvents(true)).thenReturn(eventsList);
        when(eventsList.setOrderBy(anyString())).thenReturn(eventsList);
        when(eventsList.execute()).thenThrow(new IOException("Calendar API error"));
        
        // Act & Assert
        assertThrows(CalendarServiceException.class, () -> {
            calendarService.isTimeSlotAvailable("2025-06-14", "15:30");
        });
    }
    
    @Test
    void testBookAppointment_Success() throws IOException {
        // Arrange
        BookingRequest request = new BookingRequest(
            "John Doe", "2025-06-14", "15:30", 
            "+919876543210", "john@example.com", "Consultation"
        );
        
        Events emptyEvents = new Events();
        emptyEvents.setItems(Collections.emptyList());
        
        when(calendar.events()).thenReturn(events);
        when(events.list(anyString())).thenReturn(eventsList);
        when(eventsList.setTimeMin(any())).thenReturn(eventsList);
        when(eventsList.setTimeMax(any())).thenReturn(eventsList);
        when(eventsList.setSingleEvents(true)).thenReturn(eventsList);
        when(eventsList.setOrderBy(anyString())).thenReturn(eventsList);
        when(eventsList.execute()).thenReturn(emptyEvents);
        
        Calendar.Events.Insert insertRequest = mock(Calendar.Events.Insert.class);
        when(events.insert(anyString(), any())).thenReturn(insertRequest);
        when(insertRequest.execute()).thenReturn(new com.google.api.services.calendar.model.Event());
        
        // Act & Assert
        assertDoesNotThrow(() -> calendarService.bookAppointment(request));
    }
}

// src/test/java/com/example/calendarbooking/controller/BookingControllerTest.java
package com.example.calendarbooking.controller;

import com.example.calendarbooking.dto.AvailabilityRequest;
import com.example.calendarbooking.dto.BookingRequest;
import com.example.calendarbooking.service.CalendarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CalendarService calendarService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testCheckAvailability_Available() throws Exception {
        // Arrange
        AvailabilityRequest request = new AvailabilityRequest("2025-06-14", "15:30");
        when(calendarService.isTimeSlotAvailable(anyString(), anyString())).thenReturn(true);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation").value("yes"))
                .andExpect(jsonPath("$.alternateTimeSlots").isEmpty());
    }
    
    @Test
    void testCheckAvailability_NotAvailable() throws Exception {
        // Arrange
        AvailabilityRequest request = new AvailabilityRequest("2025-06-14", "15:30");
        when(calendarService.isTimeSlotAvailable(anyString(), anyString())).thenReturn(false);
        when(calendarService.findAlternateTimeSlots(anyString()))
            .thenReturn(Arrays.asList("14:00", "16:00", "17:30"));
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation").value("no"))
                .andExpect(jsonPath("$.alternateTimeSlots").isArray())
                .andExpect(jsonPath("$.alternateTimeSlots[0]").value("14:00"));
    }
    
    @Test
    void testBookAppointment_Success() throws Exception {
        // Arrange
        BookingRequest request = new BookingRequest(
            "John Doe", "2025-06-14", "15:30",
            "+919876543210", "john@example.com", "Consultation"
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Booked"))
                .andExpect(jsonPath("$.message").value(
                    "Your appointment has been successfully scheduled for 2025-06-14 at 15:30"));
    }
    
    @Test
    void testBookAppointment_ValidationError() throws Exception {
        // Arrange - Invalid request with missing required fields
        BookingRequest request = new BookingRequest();
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}

// ==============================================================================
// Docker Configuration
// ==============================================================================

// Dockerfile
FROM openjdk:17-jdk-slim

LABEL maintainer="your-email@example.com"
LABEL description="Calendar Booking API"

WORKDIR /app

COPY target/calendar-booking-api-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

// docker-compose.yml
version: '3.8'

services:
  calendar-booking-api:
    build: .
    ports:
      - "8080:8080"
    environment:
      - GOOGLE_CALENDAR_ID=${GOOGLE_CALENDAR_ID}
      - GOOGLE_SERVICE_ACCOUNT_KEY_PATH=/app/service-account-key.json
      - GOOGLE_CALENDAR_TIMEZONE=Asia/Kolkata
    volumes:
      - ./service-account-key.json:/app/service-account-key.json:ro
    restart: unless-stopped

// ==============================================================================
// API Documentation & Setup Instructions
// ==============================================================================

// README.md
# Calendar Booking API

A Spring Boot backend service that integrates with Google Calendar to manage appointment scheduling.

## Features

- **Check Availability**: Verify if a time slot is available
- **Book Appointments**: Schedule appointments with user details
- **Google Calendar Integration**: Seamless integration with Google Calendar API
- **Input Validation**: Comprehensive validation for all inputs
- **Error Handling**: Robust error handling with meaningful responses
- **Time Zone Support**: Configurable time zone (default: IST)
- **Alternate Time Slots**: Suggest alternative times when requested slot is unavailable

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Google Cloud Platform account
- Google Calendar API enabled
- Service Account credentials

## Setup Instructions

### 1. Google Cloud Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google Calendar API
4. Create a Service Account:
   - Go to IAM & Admin > Service Accounts
   - Create new service account
   - Generate JSON key file
   - Download and save as `service-account-key.json`

### 2. Google Calendar Setup

1. Open Google Calendar
2. Create a new calendar or use existing one
3. Share the calendar with your service account email
4. Give "Make changes to events" permission
5. Copy the Calendar ID from calendar settings

### 3. Application Configuration

Update `src/main/resources/application.properties`:

```properties
google.calendar.calendar-id=your-calendar-id@group.calendar.google.com
google.calendar.service-account-key-path=/path/to/service-account-key.json
google.calendar.time-zone=Asia/Kolkata
google.calendar.slot-duration-minutes=30
```

### 4. Build and Run

```bash
# Clone repository
git clone <repository-url>
cd calendar-booking-api

# Build application
mvn clean package

# Run application
java -jar target/calendar-booking-api-1.0.0.jar
```

## API Endpoints

### Check Availability
```http
POST /api/v1/check-availability
Content-Type: application/json

{
  "date": "2025-06-14",
  "time": "15:30"
}
```

**Response (Available):**
```json
{
  "confirmation": "yes",
  "alternateTimeSlots": []
}
```

**Response (Not Available):**
```json
{
  "confirmation": "no",
  "alternateTimeSlots": ["14:00", "16:00", "17:30"]
}
```

### Book Appointment
```http
POST /api/v1/book
Content-Type: application/json

{
  "name": "John Doe",
  "date": "2025-06-14",
  "time": "15:30",
  "phoneNum": "+919876543210",
  "email": "john.doe@example.com",
  "purposeOfVisit": "Consultation"
}
```

**Response (Success):**
```json
{
  "status": "Booked",
  "message": "Your appointment has been successfully scheduled for 2025-06-14 at 15:30"
}
```

**Response (Failed):**
```json
{
  "status": "Failed",
  "message": "The selected time slot is no longer available. Please try another time."
}
```

## Validation Rules

- **Date**: Must be in YYYY-MM-DD format
- **Time**: Must be in HH:MM format (24-hour)
- **Phone**: Must start with + and contain 10-15 digits
- **Email**: Must be valid email format
- **Name**: 2-100 characters
- **Purpose**: 5-200 characters

## Error Handling

The API provides comprehensive error handling:

- **400 Bad Request**: Invalid input data
- **409 Conflict**: Time slot unavailable
- **500 Internal Server Error**: Calendar service issues

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn integration-test

# Generate test coverage report
mvn jacoco:report
```

## Docker Deployment

```bash
# Build Docker image
docker build -t calendar-booking-api .

# Run with Docker Compose
docker-compose up -d
```

## Security Considerations

- Service account credentials should be stored securely
- Use environment variables for sensitive configuration
- Implement rate limiting for production use
- Add authentication/authorization as needed
- Validate and sanitize all inputs

## Monitoring

The application includes Spring Boot Actuator endpoints:

- `/actuator/health` - Health check
- `/actuator/info` - Application information

## Troubleshooting

### Common Issues

1. **Authentication Error**: Verify service account key path and permissions
2. **Calendar Not Found**: Check calendar ID and sharing permissions
3. **Time Zone Issues**: Ensure consistent time zone configuration
4. **Rate Limiting**: Google Calendar API has usage limits

### Logs

Check application logs for detailed error information:
```bash
tail -f logs/application.log
```

## Future Enhancements

- Rate limiting implementation
- Email/SMS notifications
- Admin dashboard
- Multiple calendar support
- Recurring appointments
- Cancellation functionality
- Frontend integration

## Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## License

This project is licensed under the MIT License.
