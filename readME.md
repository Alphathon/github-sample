# 📚 GitHub Search Web Application

A **Spring Boot + Java 11 + WebFlux** app to search GitHub repositories for your organization — with paginated results, PR keyword checks, and full data download as CSV.  
Optimized for **authenticated company GitHub accounts** with **parallel PR fetching**.

---

## ✨ Features

✅ Search GitHub repos within your org with any keyword  
✅ Paginated results with total pages  
✅ For each repo:  
  - List **open PRs** that match your keyword in diffs/files  
  - Get **last commit author** and **repo owner**  
✅ Download **all matching results** as CSV  
✅ Uses **GitHub Personal Access Token (PAT)** for higher rate limits  
✅ Fast: async & parallel HTTP calls with **Spring WebClient**

---

## 🚀 Tech Stack

- Java 11
- Spring Boot 2.7.x
- Spring WebFlux
- Gradle
- Lombok

---

## ⚙️ Setup

### 1️⃣ Clone the project

```bash
git clone https://github.com/<your-username>/github-search-app.git
cd github-search-app

2️⃣ Add your GitHub PAT
Edit src/main/resources/application.yml:

server:
  port: 8080

github:
  api-url: "https://api.github.com"
  token: "ghp_your_PERSONAL_ACCESS_TOKEN_here"
  per-page: 10

3️⃣ Run the app
bash
Copy
Edit
./gradlew bootRun

Your server will run on: http://localhost:8080

🔎 API Endpoints
🔹 GET /api/github/search
Search repositories with pagination.

Example:

perl
Copy
Edit
GET /api/github/search?org=my-org&query=my-keyword&page=1

Response:

json
Copy
Edit
{
  "items": [ ... ],
  "totalPages": 5,
  "totalCount": 45
}
🔹 GET /api/github/download
Download all matching results for your search as CSV.

Example:

perl
Copy
Edit
GET /api/github/download?org=my-org&query=my-keyword
CSV Output Example:

sql
Copy
Edit
Name,URL,Description,Language,Stars,PR Title,PR URL,Last Commit User,Repo Owner
...
