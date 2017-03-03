package com.axelor.dashboard.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import com.axelor.db.JpaSupport;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.sales.db.Order;
import com.axelor.sales.db.repo.OrderRepository;

public class DashboardController extends JpaSupport {

	@Inject
	OrderRepository orderRepo;
	
	public void salesPerDay(ActionRequest req,ActionResponse res) {
		
		BigDecimal totalToday = BigDecimal.ZERO;
		BigDecimal totalYesterday = BigDecimal.ZERO;
		BigDecimal percentage = BigDecimal.ZERO;
		BigDecimal diffTodayYesterday = BigDecimal.ZERO;
		Boolean profit;
		List<Object> data = new ArrayList<Object>();

		List<Order> orderDetailsToday = orderRepo.all()
				.filter("self.orderDate = DATE(current_date) ")
				.fetch();
		List<Order> orderDetailsyesterday = orderRepo.all()
				.filter("self.orderDate = DATE(current_date) -1")
				.fetch();

		for (Order order : orderDetailsToday) {
			totalToday = totalToday.add(order.getTotalAmount());
		}
		System.out.println("Today:" + totalToday);

		for (Order order : orderDetailsyesterday) {
			totalYesterday = totalYesterday.add(order.getTotalAmount());
		}
		System.out.println("Yesterday:" + totalYesterday);

		/*
		 * yesterday 10 today 20 perc = (today - yesterday)*100 % yesterday ->
		 * 100% profit
		 */

		if (totalYesterday.compareTo(BigDecimal.ZERO) > 0) {
			diffTodayYesterday = totalToday.subtract(totalYesterday);
			percentage = (diffTodayYesterday.multiply(BigDecimal.valueOf(100))).divide(totalYesterday, 2,
					RoundingMode.HALF_UP);
			System.out.println("Percentage" + percentage + "%");
		}

		if (percentage.compareTo(BigDecimal.ZERO) > 0) {
			profit = true;
		} else {
			profit = false;
		}

		data.add(totalToday);
		data.add(percentage + "%");
		data.add(profit);

		res.setData(data);
	}
	
	public void salesPerMonth(ActionRequest req,ActionResponse res) {
		
		BigDecimal totalMonth = BigDecimal.ZERO;
		BigDecimal totalLastMonth = BigDecimal.ZERO;
		BigDecimal percentage = BigDecimal.ZERO;
		BigDecimal diffMonth = BigDecimal.ZERO;
		Boolean profit;
		List<Object> data = new ArrayList<Object>();
		List<Order> orderDetailsyesterday = new ArrayList<>();
		List<Order> orderDetailsToday = orderRepo.all()
				.filter("YEAR(self.orderDate) = YEAR(current_date) and MONTH(self.orderDate) = MONTH(current_date)")
				.fetch();
		
		Calendar c = Calendar.getInstance();
		if (c.get(Calendar.MONTH) == 0) {
			
			orderDetailsyesterday = orderRepo.all()
					.filter("YEAR(self.orderDate) = YEAR(current_date) - 1 and MONTH(self.orderDate) = 12")
					.fetch();
		} else {
			orderDetailsyesterday= orderRepo.all()
					.filter("YEAR(self.orderDate) = YEAR(current_date) and MONTH(self.orderDate)  = MONTH(current_date) - 1")
					.fetch();
		}
		
		for (Order order : orderDetailsToday) {
			totalMonth = totalMonth.add(order.getTotalAmount());
		}
		System.out.println("Today:" + totalMonth);

		for (Order order : orderDetailsyesterday) {
			totalLastMonth = totalLastMonth.add(order.getTotalAmount());
		}
		System.out.println("Yesterday:" + totalLastMonth);

		/*
		 * yesterday 10 today 20 perc = (today - yesterday)*100 % yesterday ->
		 * 100% profit
		 */
		if (totalLastMonth.compareTo(BigDecimal.ZERO) > 0) {
			diffMonth = totalMonth.subtract(totalLastMonth);
			percentage = (diffMonth.multiply(BigDecimal.valueOf(100))).divide(totalLastMonth, 2, RoundingMode.HALF_UP);
			System.out.println("Percentage" + percentage + "%");
		} else {
			percentage = BigDecimal.valueOf(100);
		}

		if (percentage.compareTo(BigDecimal.ZERO) > 0) {
			profit = true;
		} else {
			profit = false;
		}

		data.add(totalMonth);
		data.add(percentage + "%");
		data.add(profit);

		res.setData(data);
	}
	
	
	
	
	
}
