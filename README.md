# 🦡 UW-Madison Course Sniper

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green.svg)](https://spring.io/projects/spring-boot)
[![Status](https://img.shields.io/badge/Status-Active-brightgreen.svg)]()
[![License](https://img.shields.io/badge/License-MIT-blue.svg)]()

**A high-performance, WAF-resistant course enrollment monitor for University of Wisconsin-Madison students.**

> **Note:** This tool is intended for personal use and educational purposes only. Please use responsibly and respect the university's IT policies.

## 📖 Introduction

**Course Sniper** is designed to solve the pain point of securing popular courses (e.g., CS 577, CS 571) during enrollment season. Unlike simple scripts, this system is engineered to bypass strict WAF (Web Application Firewall) rules using randomized jitter, course-level aggregation, and intelligent task scheduling.

It allows users to "snipe" specific sections of a course, sending real-time email alerts immediately upon availability detection.

## ✨ Key Features

* **🛡️ Anti-WAF Architecture**:
    * **Course-Level Aggregation**: Groups multiple sections of the same course into a single API request to minimize traffic.
    * **Randomized Jitter**: Implements non-deterministic sleep intervals between requests to simulate human behavior.
    * **Lazy Loading**: Only monitors tasks explicitly enabled by the user; idle tasks consume zero network resources.
* **🎯 Precision Sniping**: Monitor status at the **Section ID** level (e.g., "Monitor Section 60035 only"), avoiding spam from unwanted sections.
* **🔍 Search & Auto-Bind**: Integrated with UW-Madison's Search API. Users can search for a course (e.g., "COMP SCI 577") and automatically import all its sections into the database.
* **💻 Interactive Dashboard**: A web-based UI to visualize course status (Open/Waitlisted/Closed), toggle monitoring switches, and manage tasks.
* **📧 Instant Alerts**: Sends email notifications via SMTP when a target section opens up.

## 🛠️ Tech Stack

* **Backend**: Java 21, Spring Boot (Web, JPA, Mail, Scheduled)
* **Database**: MySQL (Production ready)
* **Crawler**: Jsoup, Jackson (Payload construction & DOM/JSON parsing)
* **Frontend**: HTML5, CSS3, Vanilla JavaScript, Axios
* **Build Tool**: Maven

## 📸 Screenshots

*(You can add screenshots here later. Save images to a 'docs' folder and link them like: `![Dashboard](docs/dashboard.png)`) *

## 🚀 Getting Started

### Prerequisites
* JDK 21+
* Maven 3.6+

### Configuration
Update `src/main/resources/application.properties` with your settings:

```properties
# Email Settings (for alerts)
spring.mail.host=smtp.gmail.com
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Crawler Settings
monitor.poll-interval-ms=60000  # Global cycle delay
uw-api.term-id=1264             # Current Term ID
```

### Running the Application

1.  **Clone the repository**
    ```bash
    git clone [https://github.com/YOUR_USERNAME/uw-madison-course-sniper.git](https://github.com/YOUR_USERNAME/uw-madison-course-sniper.git)
    cd uw-madison-course-sniper
    ```

2.  **Build and Run**
    ```bash
    mvn spring-boot:run
    ```

3.  **Access Dashboard**
    Open your browser and navigate to: `http://localhost:8080`

## 🕹️ Usage Guide

1.  **Add a Course**:
    * Enter the course name (e.g., `COMP SCI 577`) in the search bar.
    * Click **Snipe!**. The system will fetch all sections for this course.
2.  **Toggle Monitoring**:
    * All new sections are `Disabled` by default (Safety First).
    * Find your target section (e.g., `76101`) and toggle the switch to **ON**.
3.  **Receive Alerts**:
    * Keep the application running. You will receive an email immediately when the status changes to `OPEN` or `WAITLISTED`.

## ⚠️ Disclaimer

This software is an independent project and is not affiliated with the University of Wisconsin-Madison. The developer assumes no liability for any consequences resulting from the use of this tool, including but not limited to IP bans or missed enrollments.

## 📄 License

This project is licensed under the MIT License.