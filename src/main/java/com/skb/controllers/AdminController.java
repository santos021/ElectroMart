package com.skb.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.apache.logging.log4j.message.ReusableMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.skb.ElectroMartApplication;
import com.skb.model.Category;
import com.skb.model.Product;
import com.skb.model.ProductOrder;
//import com.skb.model.SliderImage;
import com.skb.model.UserDtls;
import com.skb.service.CartService;
import com.skb.service.CategoryService;
import com.skb.service.OrderService;
import com.skb.service.ProductService;
//import com.skb.service.SliderImageService;
import com.skb.service.UserService;
import com.skb.service.impl.CategoryServiceImpl;
import com.skb.util.CommonUtil;
import com.skb.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final ElectroMartApplication electroMartApplication;

	private final CategoryServiceImpl categoryServiceImpl;

	@Autowired
	private CategoryService categoryService;

	AdminController(CategoryServiceImpl categoryServiceImpl, ElectroMartApplication electroMartApplication) {
		this.categoryServiceImpl = categoryServiceImpl;
		this.electroMartApplication = electroMartApplication;
	}

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
//	@Autowired
//	private SliderImageService sliderImageService;

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/")
	public String index() {
		return "admin/index";
	}

	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {
		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);
		return "admin/add_product";
	}

	@GetMapping("/category")
	public String category(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		// m.addAttribute("categorys", categoryService.getAllCategory());

		Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);
		List<Category> categorys = page.getContent();
		m.addAttribute("categorys", categorys);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/category";
	}

	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
		category.setImageName(imageName);

		Boolean existCategory = categoryService.existCategory(category.getName());
		if (existCategory) {
			session.setAttribute("errorMsg", "Category Name already exists");
		} else {

			Category saveCategory = categoryService.saveCategory(category);

			if (ObjectUtils.isEmpty(saveCategory)) {
				session.setAttribute("errorMsg", "Not Saved ! internal server error");
			} else {

				File saveFile = new ClassPathResource("static/images").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());
				// System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				session.setAttribute("succMsg", "Saved Successfully");
			}
		}

		return "redirect:/admin/category";
	}

	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {

		Boolean deleteCategory = categoryService.deleteCategory(id);

		if (deleteCategory) {
			session.setAttribute("succMsg", "category delete success");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {

		m.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		Category oldCategory = categoryService.getCategoryById(category.getId());
		String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();

		if (!ObjectUtils.isEmpty(category)) {
			oldCategory.setName(category.getName());
			oldCategory.setIsActive(category.getIsActive());
			oldCategory.setImageName(imageName);

		}

		Category updateCategory = categoryService.saveCategory(oldCategory);

		if (!ObjectUtils.isEmpty(updateCategory)) {

			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/images").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());
				// System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			session.setAttribute("succMsg", "Category update Successfully!");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/loadEditCategory/" + category.getId();
	}

	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			HttpSession session) throws IOException {

		String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();

		product.setImage(imageName);

		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());

		Product saveProduct = productService.saveProduct(product);

		if (!ObjectUtils.isEmpty(saveProduct)) {

			File saveFile = new ClassPathResource("static/images").getFile();

			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
					+ image.getOriginalFilename());
			// System.out.println(path);
			Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			session.setAttribute("succMsg", "Product Saved Successfully!");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/loadAddProduct";
	}

	@GetMapping("/products")
	public String loadViewProduct(Model m, @RequestParam(defaultValue = "") String ch,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

//		List<Product> products = null;
//		if (ch != null && ch.length() > 0) {
//			products = productService.searchProduct(ch);
//		} else {
//			products = productService.getAllProducts();
//		}
//		m.addAttribute("products", products);

		Page<Product> page = null;
		if (ch != null && ch.length() > 0) {
			page = productService.searchProductPagination(pageNo, pageSize, ch);
		} else {
			page = productService.getAllProductsPagination(pageNo, pageSize);
		}
		m.addAttribute("products", page.getContent());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/products";
	}

	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {

		Boolean deleteProduct = productService.deleteProduct(id);
		if (deleteProduct) {
			session.setAttribute("succMsg", "Product delete success");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/products";
	}

	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m) {

		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());

		return "admin/edit_product";
	}

	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image, Model m,
			HttpSession session) {

		if (product.getDiscount() < 0 || product.getDiscount() > 100) {
			session.setAttribute("errorMsg", "invalid Discount");
		} else {

			Product updateProduct = productService.updateProduct(product, image);
			if (!ObjectUtils.isEmpty(updateProduct)) {
				session.setAttribute("succMsg", "Product update success");
			} else {
				session.setAttribute("errorMsg", "Something wrong on server");
			}
		}
		return "redirect:/admin/editProduct/" + product.getId();
	}

	@GetMapping("/users")
	public String getAllUsers(Model m,@RequestParam Integer type) {
		List<UserDtls> users = null;
		if (type==1) {
			users = userService.getUsers("ROLE_USER");
		}else {
			users = userService.getUsers("ROLE_ADMIN");
		}
		m.addAttribute("userType",type);
		
		m.addAttribute("users", users);
		return "/admin/users";
	}

	@GetMapping("/updateSts")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id,@RequestParam Integer type, HttpSession session) {

		Boolean f = userService.updateAccountStatus(id, status);
		if (f) {
			session.setAttribute("succMsg", "Account Status Updated");
		} else {
			session.setAttribute("errorMsg", "Something went on server");
		}
		return "redirect:/admin/users?type="+type;
	}

	@GetMapping("/orders")
	public String getAllOrders(Model m, @RequestParam(defaultValue = "") String ch,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
//		List<ProductOrder> allOrders = orderService.getAllOrders();
//		m.addAttribute("orders", allOrders);
//		m.addAttribute("srch", false);
		
		Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo,pageSize);
		m.addAttribute("orders", page.getContent());
		m.addAttribute("srch", false);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "/admin/orders";
	}

	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}

		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);

		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status Updated");
		} else {
			session.setAttribute("errorMsg", "status not updated");
		}
		// System.out.println("Values"+values);

		return "redirect:/admin/orders";
	}

	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId, Model m, HttpSession session,@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

		if (orderId != null && orderId.length() > 0) {

			ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());

			if (ObjectUtils.isEmpty(order)) {
				session.setAttribute("errorMsg", "Incorrect orderId");
				m.addAttribute("orderDtls", null);
			} else {
				m.addAttribute("orderDtls", order);
			}

			m.addAttribute("srch", true);
		} else {
//			List<ProductOrder> allOrders = orderService.getAllOrders();
//			m.addAttribute("orders", allOrders);
//			m.addAttribute("srch", false);
			
			Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo,pageSize);
			m.addAttribute("orders", page);
			m.addAttribute("srch", false);
			

			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst", page.isFirst());
			m.addAttribute("isLast", page.isLast());
		}
		return "/admin/orders";
	}
	
	@GetMapping("/add-admin")
	public String loadAdminAdd() {
		return "/admin/add_admin";
	}
	
	public String product(@PathVariable int id,Model m) {
		Product productById = productService.getProductById(id);
		m.addAttribute("product",productById);
		return "view_product";
	}
	
	@PostMapping("/save-admin")
	public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("profile_image") MultipartFile file,HttpSession session) throws IOException 
	{
		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		user.setProfileImage(imageName);
		UserDtls saveUser = userService.saveAdmin(user);
		
		if (!ObjectUtils.isEmpty(saveUser)) {
			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/images").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ file.getOriginalFilename());
				//System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				session.setAttribute("succMsg", "Register Successfully");
			}
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}
		return "redirect:/admin/add-admin";
	}
	
	@GetMapping("/profile")
	public String profile() {
		return "/admin/profile";
	}
	
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user,@RequestParam MultipartFile img,HttpSession session) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not Updated");
		} else {
			session.setAttribute("succMsg", "Profile Updated");
		}
		return "redirect:/admin/profile";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword,@RequestParam String currentPassword,Principal p,HttpSession session) {
		
		UserDtls loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);
		
		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());
		
		if(matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDtls updateUser = userService.updateUser(loggedInUserDetails);
			
			if (ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", "Password not updated !! Error in server.");
			}else {
				session.setAttribute("succMsg", "Password Updated Successfully");
			}
		} else {
			session.setAttribute("errorMsg", "Current Password incorrect");
		}
		
		return "redirect:/admin/profile";
	}
	
	// ---slider image controller---
	
//	@GetMapping("slider-image")
//	public String sliderImage() {
//		return "/admin/slider_image";
//	}
//	
//	@PostMapping("/slider-image/upload")
//	public String uploadSliderImage(@RequestParam("file") MultipartFile file,
//	                                @RequestParam("title") String title,
//	                                HttpSession session) {
//	    try {
//	        String uploadDir = "src/main/resources/static/images/slider_img/";
//	        File saveDir = new File(uploadDir);
//	        if (!saveDir.exists()) saveDir.mkdirs();
//
//	        String originalFilename = file.getOriginalFilename();
//	        Path path = Paths.get(uploadDir + originalFilename);
//	        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//	        SliderImage sliderImage = new SliderImage();
//	        sliderImage.setTitle(title);
//	        sliderImage.setImageName(originalFilename);
//	        sliderImageService.save(sliderImage);
//
//	        session.setAttribute("succMsg", "Image uploaded successfully!");
//	    } catch (IOException e) {
//	        session.setAttribute("errorMsg", "Failed to upload image.");
//	        e.printStackTrace();
//	    }
//	    return "redirect:/admin/slider-image";
//	}

}
