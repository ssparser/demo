package com.example.demo.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LinkedInLoginService {

    private WebDriver driver;
    private boolean isSessionActive = false;

    private final String email;
    private final String password;

    public LinkedInLoginService() {
        Dotenv dotenv = Dotenv.load();
        this.email = dotenv.get("LINKEDIN_EMAIL");
        this.password = dotenv.get("LINKEDIN_PASSWORD");
    }

    public void loginToLinkedIn() {
        // Use WebDriverManager to manage the ChromeDriver binary
        WebDriverManager.chromedriver().setup();

        // Set ChromeOptions to run Chrome in headless mode
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu"); // Applicable to Windows OS
        options.addArguments("--no-sandbox"); // Bypass OS security model
        options.addArguments("--disable-dev-shm-usage"); // Overcome limited resource problems

        // Initialize ChromeDriver with options
        driver = new ChromeDriver(options);

        try {
            // Navigate to LinkedIn login page
            driver.get("https://www.linkedin.com/login");

            // Find username and password input elements and submit button
            WebElement usernameField = driver.findElement(By.id("username"));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

            // Enter username and password
            usernameField.sendKeys(email);
            passwordField.sendKeys(password);

            // Click on the login button
            loginButton.click();

            // Wait for the LinkedIn feed page to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.urlContains("/feed/"));

            // Check if login was successful by verifying the URL
            boolean isLoggedIn = driver.getCurrentUrl().contains("/feed/");

            if (isLoggedIn) {
                System.out.println("Login successful!");
                isSessionActive = true;

                // Keep the session active
                keepSessionActive();
            } else {
                System.out.println("Login failed!");
                isSessionActive = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            isSessionActive = false;
        }
    }

    private void keepSessionActive() {
        // Create a thread to keep the WebDriver session active
        new Thread(() -> {
            try {
                while (true) {
                    if (driver != null) {
                        // Refresh the page to keep the session active
                        driver.navigate().refresh();
                    }
                    Thread.sleep(300000); // Sleep for 5 minutes
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // Close the browser
                if (driver != null) {
                    driver.quit();
                }
                isSessionActive = false;
            }
        }).start();
    }

    public boolean isSessionActive() {
        return isSessionActive;
    }

    public List<String> fetchFeed() {
        if (!isSessionActive || driver == null) {
            throw new IllegalStateException("Session is not active.");
        }

        // Navigate to the LinkedIn feed page
        driver.get("https://www.linkedin.com/feed/");

        // Wait for the feed to load
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'scaffold-finite-scroll__content')]")));

        // Fetch feed items
        List<WebElement> feedItems = driver.findElements(By.xpath("//div[contains(@class, 'feed-shared-update-v2')]"));

        // Extract text from feed items
        return feedItems.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public void endSession() {
        if (driver != null) {
            driver.quit();
        }
        isSessionActive = false;
    }
}
