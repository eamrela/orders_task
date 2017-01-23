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
import com.jumia.task.orders.domain.Product;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.joda.time.Months;

/**
 *
 * @author Amr
 */
public class RequestHandler {
    
    private static final String PERSISTENCE_UNIT_NAME = "ordersPU";
    private static final String DATE_REGEX = "\\d+-\\d+-\\d+ \\d+:\\d+:\\d+";
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
        if(param[0].toLowerCase().contains("-list")){ // Interval List using the input paramter -list
            for (int i = 1; i < param.length-1; i=i+2) {
                if(param[i].matches(DATE_REGEX) && param[i+1].matches(DATE_REGEX)){
                    response.append(processSingleInterval(sdf.parse(param[i]), sdf.parse(param[i+1])));
                }else{
                    response.append("Wrong date format");
                }
            }
        }else{ // Single Interval
            if(param[0].matches(DATE_REGEX) && param[1].matches(DATE_REGEX)){
                    response.append(processSingleInterval(sdf.parse(param[0]), sdf.parse(param[1])));
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
    public String processSingleInterval(Date start,Date end){

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
        CriteriaQuery<Product> itemsCriteria = builder.createQuery(Product.class);
        Root<Item> items = itemsCriteria.from(Item.class);
        itemsCriteria.select(items.get(Item_.product))
                .where(items.get(Item_.orderId).in(ordersInInterval));
    // Execute a query and get the products belonging to the order items in this interval
        TypedQuery<Product> q = em.createQuery(itemsCriteria);
        String response = "Interval: "+sdf.format(start)+" TO "+sdf.format(end)+"\n";
        response+=prepareResponse(q.getResultList());
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
        return Months.monthsBetween(new DateTime(creationDate),DateTime.now()).getMonths();
    }

    /**
     * A Method that takes a list of Products and return a String representing a grouping for these products based on the creation date. 
     * @param products a List of products 
     * @return String representing a grouping for these products based on the creation date
     */
    public String prepareResponse(List<Product> products){
        StringBuilder builder = new StringBuilder();
        TreeMap<String,Integer> productsAnalysis = new TreeMap<>();
        productsAnalysis.put("1-3", 0);
        productsAnalysis.put("4-6", 0);
        productsAnalysis.put("7-12", 0);
        productsAnalysis.put(">12", 0);
    // Loop on all products and group them by the product age in months    
        products.forEach((item) -> {
            int n = extractProductAge(item.getCreationDate());
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