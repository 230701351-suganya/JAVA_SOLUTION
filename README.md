# Smart Task Manager

## 📌 Overview
Smart Task Manager is a full-stack web application designed to help users efficiently manage their daily tasks. It provides advanced task tracking, reminders, and productivity insights to improve time management and organization.

---

## 🚀 Features
- Task creation, update, and deletion  
- Task status tracking (Pending, In-Progress, Completed, Overdue)  
- Recurring tasks (Daily, Weekly, Monthly)  
- Pause, resume, and stop recurring tasks  
- Email notifications for task creation, reminders, and overdue alerts  
- Snooze and postpone tasks via email links  
- Soft delete (Trash) with restore and permanent delete options  
- Automated schedulers using Spring `@Scheduled`  
- Task analytics (status summary, priority distribution, productivity trends)  

---

## 🛠 Tech Stack
- **Backend:** Java, Spring Boot  
- **Database:** MySQL  
- **Frontend:** HTML, CSS, JavaScript (Basic UI)  
- **Email Service:** JavaMail  
- **Scheduler:** Spring @Scheduled  

---

## 🏗 Architecture
The project follows a layered architecture:
- Controller Layer – Handles API requests  
- Service Layer – Business logic  
- Repository Layer – Database interaction  

---

## 📬 Email Notification System
- Sends reminders before deadlines (5 days, 3 days, 1 day, few hours)  
- Sends overdue alerts  
- Supports snooze and postpone actions directly via email  

---

## 📊 Analytics
- Task status summary  
- Priority distribution  
- Weekly productivity tracking  

---

## ▶️ How to Run

### 1. Clone the repository
```bash
git clone https://github.com/your-username/smart-task-manager.git
