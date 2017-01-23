/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jumia.orders;


import com.jumia.orders.statistics.RequestHandler;

/**
 *
 * @author Amr
 */
public class Main {
    
    
    public static void main(String[] args) {
        
        System.out.println("------------------------");
        System.out.println("|Jumia Statistics Tools|");
        System.out.println("------------------------\n");

//        args = new String[]{"-list","2016-01-01 00:00:00","2016-01-01 00:00:00",
//                            "2016-01-01 00:00:00","2016-04-01 00:00:00",
//                            "2016-04-01 00:00:00","2017-01-01 00:00:00"};
        
        if(args.length<2){ //  Invalid Input
            System.out.println("Please enter a valid Interval(s) using one of the below formats");
            System.out.println("java -jar orders.jar \"2016-01-01 00:00:00\" \"2017-01-01 00:00:00\"");
            System.out.println("java -jar orders.jar -list \"2016-01-01 00:00:00\" \"2017-01-01 00:00:00\" \"2016-01-01 00:00:00\" \"2017-01-01 00:00:00\" ");
        }else{ // Correct Input
            // create an instance of request handler and pass the input to it
            RequestHandler handler = new RequestHandler();
            String response = handler.handleRequest(args);
            // print the response from Request Handler
            System.out.println(response);
        }
        
    }
}
