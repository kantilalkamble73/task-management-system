package com.taskflow.controller;
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
@Controller
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
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
}