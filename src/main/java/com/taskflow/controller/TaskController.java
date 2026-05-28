package com.taskflow.controller;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.taskflow.entity.Task;
import com.taskflow.repository.TaskRepository;

import jakarta.validation.Valid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
@Controller
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
    @GetMapping("/filter")
    public String filterTasks(@RequestParam String status, Model model) {

        List<Task> filteredTasks = taskRepository.findByStatus(status);

        model.addAttribute("tasks", filteredTasks);

        model.addAttribute("totalTasks", filteredTasks.size());

        model.addAttribute("pendingTasks",
                filteredTasks.stream()
                        .filter(task -> task.getStatus().equals("Pending"))
                        .count());

        model.addAttribute("completedTasks",
                filteredTasks.stream()
                        .filter(task -> task.getStatus().equals("Completed"))
                        .count());

        model.addAttribute("highPriorityTasks",
                filteredTasks.stream()
                        .filter(task -> task.getPriority().equals("High"))
                        .count());

        return "index";
    }
    
    @GetMapping("/search")
    public String searchTask(@RequestParam("keyword") String keyword,
                             Model model) {

        model.addAttribute("tasks",
                taskRepository.findByTitleContainingIgnoreCase(keyword));

        return "index";
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("tasks", taskRepository.findAll());

        // Dashboard Counts
        long totalTasks = taskRepository.count();

        long pendingTasks = taskRepository.findAll()
                .stream()
                .filter(task -> task.getStatus().equalsIgnoreCase("Pending"))
                .count();

        long completedTasks = taskRepository.findAll()
                .stream()
                .filter(task -> task.getStatus().equalsIgnoreCase("Completed"))
                .count();

        long highPriorityTasks = taskRepository.findAll()
                .stream()
                .filter(task -> task.getPriority().equalsIgnoreCase("High"))
                .count();

        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("pendingTasks", pendingTasks);
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("highPriorityTasks", highPriorityTasks);

        return "index";
    }

    @GetMapping("/add")
    public String addTaskPage(Model model) {
        model.addAttribute("task", new Task());
        return "add-task";
    }

    @PostMapping("/save")
    public String saveTask(@Valid @ModelAttribute Task task,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {

        if(result.hasErrors()) {
            return "add-task";
        }

        taskRepository.save(task);

        redirectAttributes.addFlashAttribute("success",
                "Task Added Successfully!");

        return "redirect:/";
    }
        
    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {

        taskRepository.deleteById(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Task Deleted Successfully!"
        );

        return "redirect:/";
    }
@GetMapping("/edit/{id}")
public String editTask(@PathVariable Long id, Model model) {

    Task task = taskRepository.findById(id).orElse(null);

    model.addAttribute("task", task);

    return "edit-task";
}
@PostMapping("/update/{id}")
public String updateTask(@PathVariable Long id,
                         @Valid @ModelAttribute Task task,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {

    if(result.hasErrors()) {
        task.setId(id);
        return "edit-task";
    }

    taskRepository.save(task);

    redirectAttributes.addFlashAttribute("success",
            "Task Updated Successfully!");

    return "redirect:/";
}
@GetMapping("/download/pdf")
public ResponseEntity<InputStreamResource> downloadPdf() {

    List<Task> tasks = taskRepository.findAll();

    Document document = new Document();

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try {

        PdfWriter.getInstance(document, out);

        document.open();

        document.add(new Paragraph("TASK MANAGEMENT SYSTEM"));
        document.add(new Paragraph(" "));

        for(Task task : tasks) {

            document.add(new Paragraph(
                    "ID: " + task.getId()
            ));

            document.add(new Paragraph(
                    "Title: " + task.getTitle()
            ));

            document.add(new Paragraph(
                    "Description: " + task.getDescription()
            ));

            document.add(new Paragraph(
                    "Status: " + task.getStatus()
            ));

            document.add(new Paragraph(
                    "Priority: " + task.getPriority()
            ));

            document.add(new Paragraph(
                    "Due Date: " + task.getDueDate()
            ));

            document.add(new Paragraph(
                    "-----------------------------"
            ));

        }

        document.close();

    } catch (Exception e) {

        e.printStackTrace();

    }

    ByteArrayInputStream bis =
            new ByteArrayInputStream(out.toByteArray());

    HttpHeaders headers = new HttpHeaders();

    headers.add("Content-Disposition",
            "inline; filename=tasks.pdf");

    return ResponseEntity
            .ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new InputStreamResource(bis));
}
}