package com.axelor.sales.web;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import com.axelor.db.JpaSupport;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.sales.db.Invoice;
import com.axelor.sales.db.InvoiceItem;
import com.axelor.sales.db.Order;
import com.axelor.sales.db.OrderItem;
import com.axelor.sales.db.repo.InvoiceItemRepository;
import com.axelor.sales.db.repo.InvoiceRepository;
import com.axelor.sales.db.repo.OrderRepository;
import com.axelor.sales.service.SalesOrderInvoiceService;
import com.fasterxml.jackson.annotation.JsonFormat.Value;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SalesOrderController extends JpaSupport {

	@Inject
	SalesOrderInvoiceService service;

	@Inject
	InvoiceItemRepository invoiceItemRepo;

	@Inject
	InvoiceRepository invoiceRepo;

	@Inject
	OrderRepository orderRepo;

	@Inject
	MetaFiles files;
	
	private ActionResponse fillTotalOrder(ActionResponse res, List<OrderItem> items) {
		HashMap<String, BigDecimal> totals = service.calculateTotalOrder(items);
		Iterator<Map.Entry<String, BigDecimal>> it = totals.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, BigDecimal> pair = it.next();
			res.setValue(pair.getKey(), pair.getValue());
		}
		return res;
	}

	private ActionResponse fillTotalInvoice(ActionResponse res, List<InvoiceItem> items, Invoice invoice) {
		HashMap<String, BigDecimal> totals = service.calculateTotalInvoice(items, invoice);
		Iterator<Entry<String, BigDecimal>> it = totals.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, BigDecimal> pair = it.next();
			res.setValue(pair.getKey(), pair.getValue());
		}
		return res;
	}
	
	public void calculateOrder(ActionRequest req, ActionResponse res) {
		Order order = req.getContext().asType(Order.class);
		res = this.fillTotalOrder(res, order.getItems());
	}

	public void calculateInvoice(ActionRequest req, ActionResponse res) {
		Invoice invoice = req.getContext().asType(Invoice.class);
		res = this.fillTotalInvoice(res, invoice.getInvoiceItems(), invoice);
	}

	public void emptyDraft(ActionRequest req, ActionResponse res) {

		final Order order = req.getContext().asType(Order.class);
		final EntityManager em = getEntityManager();
		em.getTransaction().begin();

		Query query = em.createNativeQuery(
				"delete from sales_order_item as orderItem where orderItem.order_sales = " + order.getId() + "");
		query.executeUpdate();

		em.getTransaction().commit();
	}
	
	@Transactional
	public void invoiceAdd(ActionRequest req, ActionResponse res) {

		final Order order = req.getContext().asType(Order.class);
		Invoice invoice = new Invoice();
		List<InvoiceItem> invoiceItems = new ArrayList<InvoiceItem>();
		EntityManager em = getEntityManager();
		// em.getTransaction().begin();

		OrderRepository orders = new OrderRepository();
		List<Order> listOrder = orders.all().filter("self.id=?", order.getId()).fetch();

		InvoiceItem[] invoiceitemsarray = new InvoiceItem[order.getItems().size()];

		for (Order o : listOrder) {
			invoice.setInvoiceOrder(o);
			invoice.setAmount(o.getAmount());
			invoice.setInvoiceDate(o.getConfirmDate());
			invoice.setTotalAmount(o.getTotalAmount());
			invoice.setTaxAmount(o.getTaxAmount());
			invoice.setState("Draft");
			invoice.setInvoiceTotalAmount(o.getTotalAmount());

			for (int i = 0; i < o.getItems().size(); i++) {
				invoiceitemsarray[i] = new InvoiceItem();
				invoiceitemsarray[i].setProduct(o.getItems().get(i).getProduct());
				invoiceitemsarray[i].setPrice(o.getItems().get(i).getPrice());
				invoiceitemsarray[i].setQuantity(o.getItems().get(i).getQuantity());
				invoiceitemsarray[i].setTax(new HashSet<>(o.getItems().get(i).getTax()));
				invoiceItems.add(invoiceitemsarray[i]);
				for (InvoiceItem it : invoiceItems) {
					it.setInvoice(invoice);
					invoiceItemRepo.save(it);
				}
			}
		}
		invoiceRepo.save(invoice);

		Query updateMetaFileNull = em
				.createNativeQuery("update sales_invoice set image = 22 where name = '" + invoice.getName() + "'");
		updateMetaFileNull.executeUpdate();
	}

	public void invoiceDelete(ActionRequest req, ActionResponse res) {
		final Order order = req.getContext().asType(Order.class);
		final EntityManager em = getEntityManager();
		em.getTransaction().begin();

		Query query1 = em.createNativeQuery(
				"delete from sales_invoice_item_tax as tax where tax.sales_invoice_item IN (select item.id from sales_invoice_item as item where item.invoice IN (select invoice.id from sales_invoice as invoice  where invoice.name='"
						+ order.getInvoice().getName() + "'))");
		query1.executeUpdate();

		Query query2 = em.createNativeQuery(
				"delete from sales_invoice_item as item where item.invoice IN (select invoice.id from sales_invoice as invoice  where invoice.name='"
						+ order.getInvoice().getName() + "')");
		query2.executeUpdate();

		Query query3 = em.createNativeQuery(
				"delete from sales_invoice as invoice  where invoice.name='" + order.getInvoice().getName() + "'");
		query3.executeUpdate();

		em.getTransaction().commit();
	}	
	
	public void path(ActionRequest req, ActionResponse res) {
		Invoice invoice = req.getContext().asType(Invoice.class);
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		MetaFile file = invoice.getImage();
		System.out.println(file);
		if (file != null) {
			Query updateMetaFileFill = em.createNativeQuery("update meta_file set file_path = '"
					+ MetaFiles.getPath(file).toString()
					+ "' where id = (select image from sales_invoice where name = '" + invoice.getName() + "')");
			updateMetaFileFill.executeUpdate();
		} else {
			String path = "/home/axelor/.axelor/attachments/logo.png";
			Query updateMetaFileNull = em
					.createNativeQuery("update sales_invoice set image = 22 where name = '" + invoice.getName() + "'");
			updateMetaFileNull.executeUpdate();
		}
		em.getTransaction().commit();
	}
	
	public void defaultImage(ActionRequest req, ActionResponse res) throws IOException {
		Invoice invoice = req.getContext().asType(Invoice.class);
		if (invoice.getImage() == null) {
			String filePath = "/home/axelor/.axelor/attachments/logo.png";
			File file = new File(filePath);
			MetaFile metaFile = files.upload(file);
			res.setValue("image", metaFile);
		}
	}
}
