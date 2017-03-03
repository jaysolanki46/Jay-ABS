package com.axelor.sales.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import com.axelor.meta.db.MetaFile;
import com.axelor.sales.db.Invoice;
import com.axelor.sales.db.InvoiceItem;
import com.axelor.sales.db.OrderItem;
import com.axelor.sales.db.Tax;

public class SalesOrderInvoiceService {
	
	@Inject
	MetaFile file;
	
	public HashMap<String, BigDecimal> calculateTotalOrder(List<OrderItem> items){
				
		BigDecimal price;
		BigDecimal quantatity;
		BigDecimal amount;
		BigDecimal amounttemp = BigDecimal.ZERO;
		BigDecimal taxAmount = BigDecimal.ZERO;
		BigDecimal totalAmount = BigDecimal.ZERO;
		Set<Tax> taxes;	
		
		for (int i = 0; i < items.size(); i++) {
			price = items.get(i).getPrice();
			quantatity = BigDecimal.valueOf(items.get(i).getQuantity());
			taxes = items.get(i).getTax();
			amount = quantatity.multiply(price);
			amounttemp = amounttemp.add(amount);

			if (taxes == null) {
				taxAmount = taxAmount.add(BigDecimal.ZERO);
			} else {
				for (Tax tax : taxes) {
					taxAmount = taxAmount.add(amounttemp.multiply(tax.getRate()));
				}
			}
		}
		
		totalAmount = amounttemp.add(taxAmount);			
		HashMap<String, BigDecimal> amt = new HashMap<>();
		amt.put("amount", amounttemp.setScale(4, RoundingMode.HALF_UP));
		amt.put("taxAmount", taxAmount.setScale(4, RoundingMode.HALF_UP));
		amt.put("totalAmount", totalAmount.setScale(4, RoundingMode.HALF_UP));
		return amt;
	}
	
	public HashMap<String, BigDecimal> calculateTotalInvoice(List<InvoiceItem> items,Invoice invoice){
		
		BigDecimal price;
		BigDecimal quantatity;
		BigDecimal amount;
		BigDecimal amounttemp = BigDecimal.ZERO;
		BigDecimal taxAmount = BigDecimal.ZERO;
		BigDecimal totalAmount = BigDecimal.ZERO;
		
		BigDecimal disAmount = BigDecimal.ZERO;
		BigDecimal invoiceTotalAmount = BigDecimal.ZERO;
		BigDecimal tempInvoiceAmount = BigDecimal.ZERO;
		Set<Tax> taxes;		
		
		for (int i = 0; i < items.size(); i++) {
			price = items.get(i).getPrice();
			quantatity = BigDecimal.valueOf(items.get(i).getQuantity());
			taxes = items.get(i).getTax();
			amount = quantatity.multiply(price);
			amounttemp = amounttemp.add(amount);
			disAmount = invoice.getDisAmount();

			if (taxes == null) {
				taxAmount = taxAmount.add(BigDecimal.ZERO);
			} else {
				for (Tax tax : taxes) {
					taxAmount = taxAmount.add(amounttemp.multiply(tax.getRate()));
				}
			}
		}
			
		totalAmount = amounttemp.add(taxAmount); // set total amount
		tempInvoiceAmount = totalAmount;
		invoiceTotalAmount = tempInvoiceAmount.subtract(disAmount);
		HashMap<String, BigDecimal> amt = new HashMap<>();
		amt.put("disAmount", disAmount.setScale(4, RoundingMode.HALF_UP));
		amt.put("invoiceTotalAmount", invoiceTotalAmount.setScale(4, RoundingMode.HALF_UP));
		amt.put("amount", amounttemp.setScale(4, RoundingMode.HALF_UP));
		amt.put("taxAmount", taxAmount.setScale(4, RoundingMode.HALF_UP));
		amt.put("totalAmount", totalAmount.setScale(4, RoundingMode.HALF_UP));
		return amt;
	}
	
	public String imagePath() {
		return file.getFilePath();
	}
}
