/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jumia.orders.statistics;


import com.jumia.task.orders.domain.Item;
import com.jumia.task.orders.domain.Item_;
import com.jumia.task.orders.domain.Order;
import com.jumia.task.orders.domain.Order_;
import com.jumia.task.orders.domain.Product_;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 *
 * @author Amr
 */
public class RequestHandler {
    
    private static final String PERSISTENCE_UNIT_NAME = "ordersPU";
    private static final String DATE_REGEX = "\\d+-\\d+-\\d+ \\d+:\\d+:\\d+";
    private static final String FILTER_REGEX = "\\d+-\\d+";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final EntityManager em;
    
    // Initialize the Entity Manager in constructor
    public RequestHandler(){
        em = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME).createEntityManager();
    }
    
    /**
     * A Method that takes Array of strings containing intervals and return a string containing statistics on the products being sold 
     * in the below format 
     *   1-3 months: 200 orders
     *   4-6 months: 150 orders
     *   7-12 months: 50 orders
     *   12 months: 20 orders
     * @param param Array of Strings that passed by the user, containing the intervals to get statistics for. 
     * @return a String containing the result after accessing the database and doing analysis
     */
    public String handleRequest(String[] param){
        StringBuilder response = new StringBuilder();
    try {
        if(param.length>2 && param[2].toLowerCase().equals("-list")){ // Interval List using the input paramter -list
                response.append(processInterval(sdf.parse(param[0]), sdf.parse(param[1]),param));
        }else{ // Single Interval
            if(param[0].matches(DATE_REGEX) && param[1].matches(DATE_REGEX)){
                    response.append(processInterval(sdf.parse(param[0]), sdf.parse(param[1])));
            }else{
                response.append("Wrong date format");
            } 
        }
        } catch (ParseException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    
        return response.toString();
    }

    
    /**
     * A Method that create a Criteria Query with JPA Specifications and execute it to get the products analysis during a certain interval
     * @param start The Start of the Interval
     * @param end The End of the Interval
     * @return  A String representing the result of the analysis
     */
    public String processInterval(Date start,Date end){

    // Get Instance from Criteria Builder
        CriteriaBuilder builder = em.getCriteriaBuilder();
    // Create a criteria for the needed interval 
        CriteriaQuery<Order> ordersCriteria = builder.createQuery(Order.class);
        Root<Order> orders = ordersCriteria.from(Order.class);
        Predicate predicate = builder.between(orders.get(Order_.orderDate),start,end);
    // prepare the query
        ordersCriteria.select(orders).where(predicate);
    // execute the query to check the orders in the given interval
        List<Order> ordersInInterval = em.createQuery(ordersCriteria).getResultList();
    // if there are orders in this interval check the items and products 
        if(!ordersInInterval.isEmpty()){      
    // Create criteria for the items based on the Orders in the interval    
        CriteriaQuery<Date> itemsCriteria = builder.createQuery(Date.class);
        Root<Item> items = itemsCriteria.from(Item.class);
        itemsCriteria.select((items.get(Item_.product)).get(Product_.creationDate))
                .where(items.get(Item_.orderId).in(ordersInInterval));
    // Execute a query and get the products belonging to the order items in this interval
        TypedQuery<Date> q = em.createQuery(itemsCriteria);
        String response = "Interval: "+sdf.format(start)+" TO "+sdf.format(end)+"\n";
        response+=prepareResponse(q.getResultList(),null);
        return response;
        }
        return "No Orders made during this Interval ("+sdf.format(start)+" TO "+sdf.format(end)+")\n";
    }
    
    /**
     * A Method that create a Criteria Query with JPA Specifications and execute it to get the products analysis during a certain interval
     * @param start The Start of the Interval
     * @param end The End of the Interval
     * @param param filtering conditions
     * @return  A String representing the result of the analysis
     */
    public String processInterval(Date start,Date end,String[] param){
        
        // Get Instance from Criteria Builder
        CriteriaBuilder builder = em.getCriteriaBuilder();
    // Create a criteria for the needed interval 
        CriteriaQuery<Order> ordersCriteria = builder.createQuery(Order.class);
        Root<Order> orders = ordersCriteria.from(Order.class);
        Predicate predicate = builder.between(orders.get(Order_.orderDate),start,end);
    // prepare the query
        ordersCriteria.select(orders).where(predicate);
    // execute the query to check the orders in the given interval
        List<Order> ordersInInterval = em.createQuery(ordersCriteria).getResultList();
    // if there are orders in this interval check the items and products 
        if(!ordersInInterval.isEmpty()){      
    // Create criteria for the items based on the Orders in the interval    
        CriteriaQuery<Date> itemsCriteria = builder.createQuery(Date.class);
        Root<Item> items = itemsCriteria.from(Item.class);
    // prepare filtering condition
        List<Date> filters = new ArrayList<>();
        for (int i = 3; i < param.length; i++) {
                if(param[i].matches(FILTER_REGEX)){
                    filters.add(getDateByAge(Integer.parseInt(param[i].split("-")[0])));
                    filters.add(getDateByAge(Integer.parseInt(param[i].split("-")[1])));
                }
        }
         List<Predicate> productCondition=new ArrayList<>();
        for (int i = 0; i < filters.size(); i++) {
            productCondition.add((Predicate)builder.between((items.get(Item_.product)).get(Product_.creationDate),filters.get(i),DateTime.now().toDate()));   
        }
        itemsCriteria.select((items.get(Item_.product)).get(Product_.creationDate))
                .where(builder.and(items.get(Item_.orderId).in(ordersInInterval),
                        builder.or(productCondition.toArray(new Predicate[productCondition.size()]))));
    // Execute a query and get the products belonging to the order items in this interval
        TypedQuery<Date> q = em.createQuery(itemsCriteria);
        String response = "Interval: "+sdf.format(start)+" TO "+sdf.format(end)+"\n";
        response+=prepareResponse(q.getResultList(),param);
        return response;
        }
        return "No Orders made during this Interval ("+sdf.format(start)+" TO "+sdf.format(end)+")\n";
    }
    
    /**
     * A Method that take a date and subtract it from the current date to get the number of months in-between 
     * @param creationDate a Date 
     * @return Number of months
     */
    public int extractProductAge(Date creationDate){
        return Days.daysBetween(new DateTime(creationDate),DateTime.now()).getDays()/30;
    }
    
    /**
     * A Method that takes number of months and subtract it from current time
     * @param Age number of months
     * @return 
     */
    public Date getDateByAge(int Age){
        return DateTime.now().withTimeAtStartOfDay().minusMonths(Age).toDate();
    }

    /**
     * A Method that takes a list of Products and return a String representing a grouping for these products based on the creation date. 
     * @param products a List of products 
     * @param param Filtering conditions
     * @return String representing a grouping for these products based on the creation date
     */
    public String prepareResponse(List<Date> products,String[] param){
        StringBuilder builder = new StringBuilder();
        TreeMap<String,Integer> productsAnalysis = new TreeMap<>();
        // Prepare results holder based on filters
        if(param==null){
            productsAnalysis.put("1-3", 0);
            productsAnalysis.put("4-6", 0);
            productsAnalysis.put("7-12", 0);
            productsAnalysis.put(">12", 0);
        }else{
            for (int i = 3; i < param.length; i++) {
                productsAnalysis.put(param[i], 0);
            }
        }
    // Loop on all products and group them by the product age in months    
        products.forEach((item) -> {
            int n = extractProductAge(item);
            switch(n){
                case 0:case 1:case 2:case 3:
                        productsAnalysis.put("1-3", productsAnalysis.get("1-3")+1);
                    break;
                case 4:case 5:case 6:
                        productsAnalysis.put("4-6", productsAnalysis.get("4-6")+1);
                    break;
                case 7:case 8:case 9:case 10:case 11:case 12:
                        productsAnalysis.put("7-12", productsAnalysis.get("7-12")+1);
                    break;
                default:
                        productsAnalysis.put(">12", productsAnalysis.get(">12")+1);
                    break;
            }
        });
        
    // Loop on the TreeMap and prepare the results in the needed format
        productsAnalysis.entrySet().forEach((entry) -> {
            builder.append(entry.getKey()).append(" Months").append(": ").append(entry.getValue()).append(" Orders").append("\n");
        });
        return builder.toString();
    }
}