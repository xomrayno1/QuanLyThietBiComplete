package com.tampro.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.tampro.dto.InventoryDTO;
import com.tampro.dto.InvoiceDTO;
import com.tampro.dto.ProductDTO;
import com.tampro.dto.SupplierDTO;
import com.tampro.dto.UserDTO;
import com.tampro.report.DocumentGoodsReceipt;
import com.tampro.service.InventoryService;
import com.tampro.service.InvoiceService;
import com.tampro.service.ProductService;
import com.tampro.service.SupplierService;
import com.tampro.utils.Constant;
import com.tampro.utils.Paging;
import com.tampro.validator.InvoiceValidator;

@Controller
@RequestMapping("/goods-receipt")
public class GoodsReceiptController {
	@Autowired
	InvoiceService invoiceService;
	@Autowired
	InvoiceValidator invoiceValidator;
	@Autowired
	ProductService productService;
	@Autowired
	InventoryService inventoryService;
	@Autowired
	SupplierService supplierService;
	
	@InitBinder
	public void initBinder(WebDataBinder dataBinder) {
		if(dataBinder.getTarget() == null) {
			return;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy-MM-dd");
		dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(simpleDateFormat, true));
		if(dataBinder.getTarget().getClass() == InvoiceDTO.class) {
			dataBinder.setValidator(invoiceValidator);
		}
	}
	@GetMapping(value = {"/list/","/list"})
	public String redirect() {
		return "redirect:/goods-receipt/list/1";
	}
	@RequestMapping("/list/{page}")
	public String listInvoice(Model model,@ModelAttribute("searchForm") InvoiceDTO invoiceDTO,@PathVariable("page") int page
			,HttpSession session) {
		Paging paging = new Paging(5);
		paging.setIndexPage(page);
		invoiceDTO.setType(Constant.GOODS_RECEIPT);
		List<InvoiceDTO> list = invoiceService.getAll(invoiceDTO, paging);
		model.addAttribute("list", list);
		model.addAttribute("pageInfo", paging);
		if(session.getAttribute(Constant.MSG_ERROR) != null) {
			model.addAttribute(Constant.MSG_ERROR, session.getAttribute(Constant.MSG_ERROR));
 			session.removeAttribute(Constant.MSG_ERROR);
		}
		if(session.getAttribute(Constant.MSG_SUCCESS) != null) {
			model.addAttribute(Constant.MSG_SUCCESS, session.getAttribute(Constant.MSG_SUCCESS));
			session.removeAttribute(Constant.MSG_SUCCESS);
		}
		innitSelect(model);
		return "goods-receipt-list";
	}
	@GetMapping(value = {"/add"})
	public String addInvoice(Model model) {
		model.addAttribute("title", "Add");
		model.addAttribute("submitForm", new InvoiceDTO(Constant.GOODS_RECEIPT));
		model.addAttribute("viewOnly", false);
		innitSelect(model);
		return "goods-receipt-action";
	}
	@GetMapping(value = {"/view/{id}"})
	public String viewInvoice(Model model,@PathVariable("id") int id) {
		InvoiceDTO invoiceDTO = invoiceService.findById(id);
		model.addAttribute("title", "View");
		model.addAttribute("submitForm", invoiceDTO);
		model.addAttribute("viewOnly", true);
		return "goods-receipt-action";
	}
	
	 @GetMapping(value = {"/edit/{id}"})
	public String editInvoice(Model model,@PathVariable("id") int id) {
		InvoiceDTO invoiceDTO = invoiceService.findById(id);
		model.addAttribute("title", "Edit");
		model.addAttribute("submitForm", invoiceDTO);
		model.addAttribute("viewOnly", false);
		innitSelect(model);
		return "goods-receipt-action";
	 }
	 @PostMapping(value = {"/save"})
		public String save(Model model,@ModelAttribute("submitForm") @Validated InvoiceDTO invoiceDTO 
				, BindingResult result, HttpSession session ) {
				
			if(result.hasErrors()) {
				if(invoiceDTO.getId() != 0) {
					model.addAttribute("title", "Edit");
				}else {
					model.addAttribute("title", "Add");
				}
				innitSelect(model);
				model.addAttribute("submitForm", invoiceDTO);
				return "goods-receipt-action";
			}	
			invoiceDTO.setType(Constant.GOODS_RECEIPT);
			if(invoiceDTO.getId() != 0) {
				try {
					invoiceService.update(invoiceDTO);
					session.setAttribute(Constant.MSG_SUCCESS, "Cập nhật thành công");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					session.setAttribute(Constant.MSG_ERROR, "Cập nhật thất bại");
				}
			}else {
				try {
					UserDTO userDTO = (UserDTO)	session.getAttribute(Constant.USER_INFO);		
					invoiceDTO.setUserDTO(userDTO);
					invoiceService.add(invoiceDTO);
					session.setAttribute(Constant.MSG_SUCCESS, "Thêm thành công");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					session.setAttribute(Constant.MSG_ERROR, "Thêm thất bại");
				}
			}
			return "redirect:/goods-receipt/list/1";
		}
	public void innitSelect(Model model) {
		List<ProductDTO> list = productService.getAll(null, null);
		Collections.sort(list,new Comparator<ProductDTO>() {
			public int compare(ProductDTO o1, ProductDTO o2) {
				// TODO Auto-generated method stub
				return o1.getName().compareTo(o2.getName());
			}		
		});
		model.addAttribute("listProduct", list);
		List<InventoryDTO> listInven = inventoryService.getAll(null, null);
		Collections.sort(listInven,new Comparator<InventoryDTO>() {
			public int compare(InventoryDTO o1, InventoryDTO o2) {
				// TODO Auto-generated method stub
				return o1.getName().compareTo(o2.getName());
			}		
		}); 
		model.addAttribute("listInven", listInven);
		
		List<SupplierDTO> listSupp = supplierService.getAll(null, null);
		 
		
		Collections.sort(listSupp,new Comparator<SupplierDTO>() {
			public int compare(SupplierDTO o1, SupplierDTO o2) {
				// TODO Auto-generated method stub
				return o1.getName().compareTo(o2.getName());
			}		
		});
		model.addAttribute("listSupp", listSupp);
		
	}
	@GetMapping(value = {"/excel-file"})
	public ModelAndView exportExcelGoodReceipt( ) {
		ModelAndView modelAndView = new ModelAndView();
		 
		modelAndView.setView(new DocumentGoodsReceipt());
		List<ProductDTO> list = productService.getAll(null, null);
		Collections.sort(list,new Comparator<ProductDTO>() {
			public int compare(ProductDTO o1, ProductDTO o2) {
				// TODO Auto-generated method stub
				return o1.getCode().compareTo(o2.getCode());
			}		
		});
		 
		List<InventoryDTO> listInven = inventoryService.getAll(null, null);
		Collections.sort(listInven,new Comparator<InventoryDTO>() {
			public int compare(InventoryDTO o1, InventoryDTO o2) {
				// TODO Auto-generated method stub
				return o1.getName().compareTo(o2.getName());
			}		
		});
		 
		List<SupplierDTO> listSupplier = supplierService.getAll(null, null);
		Collections.sort(listSupplier,new Comparator<SupplierDTO>() {
			public int compare(SupplierDTO o1, SupplierDTO o2) {
				// TODO Auto-generated method stub
				return o1.getName().compareTo(o2.getName());
			}
 		
		});
		modelAndView.addObject("suppliers", listSupplier);
		modelAndView.addObject("inventory", listInven);
		modelAndView.addObject("products", list);
		return modelAndView;
	}
	@PostMapping(value = {"/import-excel"})
	public String  importExcel(Model model,@RequestParam("file") MultipartFile file,HttpSession session) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
		HSSFSheet sheet = workbook.getSheetAt(0);		
		for(int i = 0 ;	i < sheet.getPhysicalNumberOfRows() ; i++) {
			if(i  > 0) {
				HSSFRow row  = sheet.getRow(i);
				
				try {
					InvoiceDTO invoiceDTO = new InvoiceDTO();
					UserDTO userDTO = (UserDTO)	session.getAttribute(Constant.USER_INFO);		
					ProductDTO productDTO = productService.findByName(row.getCell(0).getStringCellValue());
					BigDecimal price = new BigDecimal(row.getCell(1).getNumericCellValue());
					invoiceDTO.setPrice(price);
					 
					invoiceDTO.setQuantity(new Double(row.getCell(2).getNumericCellValue()).intValue());
					InventoryDTO inventoryDTO = inventoryService.findByName(row.getCell(3).getStringCellValue());
					SupplierDTO supplierDTO = supplierService.findByName(row.getCell(4).getStringCellValue());
					
					 
					invoiceDTO.setDescription("Nhap Excel");
					invoiceDTO.setInvenId(inventoryDTO.getId());
					invoiceDTO.setProductId(productDTO.getId());
					invoiceDTO.setSuppId(supplierDTO.getId());
					invoiceDTO.setType(Constant.GOODS_RECEIPT);
					invoiceDTO.setUserDTO(userDTO);
					System.out.println(invoiceDTO);
					invoiceService.add(invoiceDTO);
					session.setAttribute(Constant.MSG_SUCCESS, "Import thành công");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					session.setAttribute(Constant.MSG_ERROR, "Import thất bại");
				}
			}
		}
		return "redirect:/goods-receipt/list/1";
	}
}
