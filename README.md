\# AI Resume Analyzer \& Job Match Assistant



An AI-powered web application that analyzes resumes, evaluates their relevance to job descriptions, and generates personalized interview questions.



\## Features



\- \*\*User Authentication:\*\* Secure registration and login using JWT.

\- \*\*Resume Analysis:\*\* Upload PDF resumes for AI-powered analysis.

\- \*\*Job Matching:\*\* Compare a resume against a job description.

\- \*\*Interview Preparation:\*\* Generate interview questions based on the resume and target job.

\- \*\*Resume Management:\*\* Save and manage analyzed resumes.

\- \*\*REST API:\*\* Backend endpoints built with Spring Boot.



\## Tech Stack



\*\*Frontend:\*\* React, Vite, Axios  

\*\*Backend:\*\* Java, Spring Boot, Spring AI  

\*\*Database:\*\* MySQL  

\*\*AI Model:\*\* Ollama (Llama 3.2)  

\*\*Security:\*\* Spring Security, JWT  

\*\*Document Processing:\*\* Apache PDFBox  

\*\*API Documentation:\*\* Swagger / OpenAPI



\## Project Structure



```text

ai-resume-analyzer/

├── ai-resume-analyzer/  # Spring Boot backend

└── frontend/            # React frontend

```



\## Prerequisites



\- Java 21 or compatible configured Java runtime

\- Node.js and npm

\- MySQL Server

\- Ollama with the required model downloaded



\## Running the Application



\### 1. Start the AI model



Install Ollama and download the model:



```bash

ollama pull llama3.2

ollama serve

```



If Ollama is already running as a background service, you do not need to start it again.



\### 2. Start the backend



Open a terminal in the `ai-resume-analyzer` backend folder.



Configure your MySQL connection and other application settings using environment variables or your local configuration file.



Run:



```powershell

.\\mvnw.cmd spring-boot:run

```



The backend runs on `http://localhost:8080` unless configured otherwise.



\### 3. Start the frontend



Open another terminal in the `frontend` folder:



```bash

npm install

npm run dev

```



Open the local URL printed by Vite, usually `http://localhost:5173`.



\## API Documentation



When the backend is running, visit:



http://localhost:8080/swagger-ui/index.html



\## Future Improvements



\- Resume scoring and skill-gap recommendations

\- Resume version comparison

\- Live deployment

\- Additional AI-powered career guidance



\## Author



Developed as a full-stack AI application using React, Spring Boot, MySQL, and Ollama.



