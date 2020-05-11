package org.sid.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.sid.dao.ClientRepository;
import org.sid.entities.Client;
import org.sid.entities.Commentaire;
import org.sid.entities.Formation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;


@Controller
public class ClientController {
	
	@Autowired
	private ClientRepository clientRepository;
	@Value("${dir.userimages}")
	private String imageDir;
	
	@RequestMapping(value="/home",method=RequestMethod.GET)
	public String logout(Model model,HttpSession session) {
		session.invalidate();
		
		return "redirect:/";
	}
	
	
	@RequestMapping(value="/about",method=RequestMethod.GET)
	public String about(Model model) {
		
		return "about-us";
	}
	
	@RequestMapping(value="/contact-us",method=RequestMethod.GET)
	public String contact(Model model,HttpSession session) {
		
		return "contact-us";
	}

	@RequestMapping(value="/Register",method=RequestMethod.GET)
	public String formProduit(Model model) {
		model.addAttribute("client",new Client());
		return "register";
	}
	@RequestMapping(value="/Registration",method=RequestMethod.POST)
	public String save(Model model ,Client client,@RequestParam(name="photo") MultipartFile picture,HttpSession session,HttpServletRequest request) throws IllegalStateException, IOException {
		client.setPicture(picture.getOriginalFilename());
		clientRepository.save(client);
		if(!(picture.isEmpty())) {
			client.setPicture(picture.getOriginalFilename());
			picture.transferTo(new File(imageDir+client.getId()));
		}
		session=request.getSession(true);
		
		session.setAttribute("user", client);
		
	
		
		return "login";
	}
	@RequestMapping(value="getUserPhoto",produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public byte[] getUserPhoto(Long id) throws FileNotFoundException, IOException {
		File f=new File(imageDir+id);
		return IOUtils.toByteArray(new FileInputStream(f));
	}
	@RequestMapping(value="/login",method=RequestMethod.GET)
	public String login(Model model) {
		
		model.addAttribute("client",new Client());
		return "login";
	}
	
	@RequestMapping(value="/Authentification",method=RequestMethod.POST)
	public String authentification(Model model  ,@RequestParam("email") String email, @RequestParam("password")  String password,HttpSession session,HttpServletRequest request) {
		List<Client> client = new ArrayList<Client>();
		client = clientRepository.findByEmail(email);
		if(client.size()==0) return "register";
		else if(!client.get(0).getPassword().equals(password)) return "loginError";
		else {
			session=request.getSession(true);
			session.setAttribute("user", client.get(0));
			
			return "redirect:TrainingManagement";
			}
	}
	@RequestMapping(value="/editTrainerProfil",method=RequestMethod.GET)
	public String editTrainerProfil(Model model,HttpServletRequest request,Long id) {
		HttpSession session=request.getSession(true);
		Client client=(Client) session.getAttribute("user");
		
		Client trainer=clientRepository.getOne(id);
		model.addAttribute("trainer", trainer);
		
		if(session.getAttribute("user")==null) return "trainer-profile-Visiteur";
		
		
		return "trainer-profile";
	}
	
	

	
	@RequestMapping(value="updatePersonnaalInfo")
	public String updatePersonnaalInfo(@RequestParam("firstName") String prenom,@RequestParam("lastName") String nom,
					@RequestParam("job") String job,@RequestParam("photo") MultipartFile file,
					@RequestParam("adresse") String adresse,@RequestParam("new-email") String email
					,@RequestParam("new-password") String password,HttpServletRequest request) throws IOException {
		HttpSession session=request.getSession(true);
		Client client=(Client) session.getAttribute("user");
		Client cli=clientRepository.getOne(client.getId());
		cli.setNom(nom);
		cli.setPrenom(prenom);
		cli.setAddress(adresse);
		cli.setJob(job);
		client.setNom(nom);
		client.setPrenom(prenom);
		client.setAddress(adresse);
		client.setJob(job);
		
		if(email.isEmpty()) {
			cli.setEmail(client.getEmail());
		}
		else {
			client.setEmail(email);
			cli.setEmail(email);
		}
		
		if(password.isEmpty()) {
			cli.setPassword(client.getPassword());
		}
		else {
			cli.setPassword(password);
			client.setPassword(password);
		}
		
		if(!(file.isEmpty())) {
			cli.setPicture(file.getOriginalFilename());
			File f=new File(imageDir+cli.getId());
			if(f.exists()) {
					byte[] bytes=file.getBytes();
					Path path=Paths.get(imageDir+cli.getId());
					Files.write(path, bytes);		
			}
			else 
				{
				
				file.transferTo(new File(imageDir+cli.getId()));
				}
			
		}
		
		clientRepository.save(cli);
		
		return "redirect:editUserProfile";
		
	}
	
	
	
	
	
	
	
	
	
	
	
	

	

}

