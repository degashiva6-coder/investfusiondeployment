package com.investfusion;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.*;
import java.util.regex.*;

@Service
public class CasParserService {
    // Best-effort parser for text-based CAS statements. Review extracted rows before importing.
    private static final Pattern ISIN = Pattern.compile("\\b(IN[EF][A-Z0-9]{9,10})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER = Pattern.compile("(?<![A-Za-z])(?:₹\\s*)?([0-9,]+(?:\\.\\d{1,4})?)");
    public List<Map<String,Object>> parse(byte[] bytes) throws IOException {
        List<Map<String,Object>> out = new ArrayList<>();
        try(PDDocument doc = Loader.loadPDF(bytes)){
            String text = new PDFTextStripper().getText(doc);
            String[] lines = text.split("\\R");
            for(String line: lines){
                Matcher im=ISIN.matcher(line);
                if(!im.find()) continue;
                String isin=im.group(1).toUpperCase(Locale.ROOT);
                String lower=line.toLowerCase(Locale.ROOT);
                List<Double> nums=new ArrayList<>();
                Matcher nm=NUMBER.matcher(line);
                while(nm.find()){
                    try { nums.add(Double.parseDouble(nm.group(1).replace(",",""))); } catch(Exception ignored){}
                }
                // The parser expects a name followed by an ISIN and at least quantity + price/value.
                if(nums.size()<2) continue;
                String name=line.substring(0, im.start()).replaceAll("[|,:;\\-]+$","").trim();
                name=name.replaceAll("^\\d+\\s+","").trim();
                if(name.length()<2) name = "Holding " + isin;
                double quantity=nums.get(Math.max(0, nums.size()-3));
                double price=nums.get(nums.size()-2);
                double value=nums.get(nums.size()-1);
                if(quantity>1_000_000_000 || price>1_000_000_000 || value>1_000_000_000) continue;
                String type=(lower.contains("fund")||lower.contains("mutual")||lower.contains("scheme")||lower.contains("folio"))?"MUTUAL_FUND":"STOCK";
                String symbol="";
                if(type.equals("STOCK")) {
                    String n=name.toLowerCase(Locale.ROOT);
                    if(n.contains("reliance"))symbol="RELIANCE.NS";
                    else if(n.contains("tata consultancy")||n.matches(".*\\btcs\\b.*"))symbol="TCS.NS";
                    else if(n.contains("infosys"))symbol="INFY.NS";
                    else if(n.contains("hdfc bank"))symbol="HDFCBANK.NS";
                    else if(n.contains("icici bank"))symbol="ICICIBANK.NS";
                }
                Map<String,Object> row=new LinkedHashMap<>();
                row.put("name",name);row.put("symbol",symbol);row.put("isin",isin);row.put("assetType",type);
                row.put("quantity",quantity);row.put("currentPrice",price);row.put("averageCost", quantity>0?value/quantity:0);
                row.put("statementValue",value);row.put("notes","Parsed from "+type+" CAS row; please verify.");
                out.add(row);
            }
        }
        return out;
    }
}
