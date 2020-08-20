package it.wtfcode.rocknet.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class MainUtil {
	
	public static CharSequence indicator = "-> ";

	public static String readString(String message) {
		String res = "";
		while(StringUtils.isBlank(res)) {
			System.out.print(message);
			res=readLine();        		
		}
		return res;
    }
    public static String readString(String message,String value) {
    	System.out.print(message+" ["+value+"] ");
		String res = readLine();
		if(StringUtils.isBlank(res)) {
			res = value;
		}
		return res;
    }
    public static boolean readBoolean(String message,boolean defaultValue) {
    	return readBoolean(message,defaultValue,"y","n");
    }
    public static boolean readBoolean(String message,boolean defaultValue,String trueValue,String falseValue) {
		trueValue = (defaultValue ? trueValue.toUpperCase(): trueValue.toLowerCase());
		falseValue = (!defaultValue ? falseValue.toUpperCase(): falseValue.toLowerCase());
		String comparisonValue = (defaultValue ? falseValue : trueValue);
		System.out.print(message + " ["+trueValue+"|"+falseValue+"] ");
		boolean res=defaultValue;
		String start = readLine();
		if(!StringUtils.isAllBlank(start)&&StringUtils.equalsIgnoreCase(start, comparisonValue)) {
			res=!defaultValue;
		}
		return res;
	}
    public static int readInt(String message,int min, int max) {
    	int ret = -1;
    	while(ret == -1) {
    		System.out.print(message);
    		String tempRes = readLine();
    		try {
    			ret = Integer.valueOf(tempRes);
    			if(min>=0) {
    				ret = (ret >= min ? ret : -1);
    			}
    			if(max>=0) {
    				ret = (ret <= max ? ret : -1);
    			}
    		}catch(NumberFormatException e) {
    			ret = -1;
    		}
    	}
    	return ret;
    }
    public static int readInt(String message) {
    	int ret = -1;
    	while(ret == -1) {
    		System.out.print(message);
    		String tempRes = readLine();
    		try {
    			ret = Integer.valueOf(tempRes);
    		}catch(NumberFormatException e) {
    			ret = -1;
    		}
    	}
    	return ret;
    }
	public static <T> T readChoose(String typeDesc,List<T> list) {
		T tempScelto = null;
		while(tempScelto == null ? true : !list.contains(tempScelto)) {
			int counter = 0;
			for(T template:list) {
				System.out.println("For "+typeDesc+": "+template.toString()+" write: "+(counter+1));
				counter ++;
			}
			tempScelto = list.get(readInt("CHOOSEN "+typeDesc.toUpperCase()+": ",1,counter+1)-1);
		}
		return tempScelto;
	}
	public static String readString(CharSequence indicator, String message) {
		String res = "";
		while(StringUtils.isBlank(res)) {
			System.out.print(message);
			res=readLine(indicator);        		
		}
		return res;
    }
    public static String readString(CharSequence indicator, String message,String value) {
    	System.out.print(message+" ["+value+"] ");
		String res = readLine(indicator);
		if(StringUtils.isBlank(res)) {
			res = value;
		}
		return res;
    }
    public static boolean readBoolean(CharSequence indicator, String message,boolean defaultValue) {
    	return readBoolean(indicator,message,defaultValue,"y","n");
    }
    public static boolean readBoolean(CharSequence indicator, String message,boolean defaultValue,String trueValue,String falseValue) {
		trueValue = (defaultValue ? trueValue.toUpperCase(): trueValue.toLowerCase());
		falseValue = (!defaultValue ? falseValue.toUpperCase(): falseValue.toLowerCase());
		String comparisonValue = (defaultValue ? falseValue : trueValue);
		System.out.print(message + " ["+trueValue+"|"+falseValue+"] ");
		boolean res=defaultValue;
		String start = readLine(indicator);
		if(!StringUtils.isAllBlank(start)&&StringUtils.equalsIgnoreCase(start, comparisonValue)) {
			res=!defaultValue;
		}
		return res;
	}
    public static int readInt(CharSequence indicator, String message,int min, int max) {
    	int ret = -1;
    	while(ret == -1) {
    		System.out.print(message);
    		String tempRes = readLine(indicator);
    		try {
    			ret = Integer.valueOf(tempRes);
    			if(min>=0) {
    				ret = (ret >= min ? ret : -1);
    			}
    			if(max>=0) {
    				ret = (ret <= max ? ret : -1);
    			}
    		}catch(NumberFormatException e) {
    			ret = -1;
    		}
    	}
    	return ret;
    }
    public static int readInt(CharSequence indicator, String message) {
    	int ret = -1;
    	while(ret == -1) {
    		System.out.print(message);
    		String tempRes = readLine();
    		try {
    			ret = Integer.valueOf(tempRes);
    		}catch(NumberFormatException e) {
    			ret = -1;
    		}
    	}
    	return ret;
    }
	public static <T> T readChoose(CharSequence indicator, String typeDesc,List<T> list) {
		T tempScelto = null;
		while(tempScelto == null ? true : !list.contains(tempScelto)) {
			int counter = 0;
			for(T template:list) {
				System.out.println("For "+typeDesc+": "+template.toString()+" write: "+(counter+1));
				counter ++;
			}
			tempScelto = list.get(readInt("CHOOSEN "+typeDesc.toUpperCase()+": ",1,counter+1)-1);
		}
		return tempScelto;
	}
	public static String readLine() {
		return readLine(indicator);
	}
    public static String readLine(CharSequence indicator) {
    	 BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    	 try {
    		System.out.print(indicator);
			return in.readLine();
		} catch (Exception e) {
			return "";
		}
    }
	public static Map<String, String> parseArgs(String[] args) {
		Map<String,String> arguments = new HashMap<>();
		if(args != null) {
			for(String arg:args) {
				String argsName = null;
				String argsValue = null;
				String[] splitted = StringUtils.split(arg, "=", 2);
				if(splitted.length==1) {
					argsName = splitted[0];
				}else if(splitted.length == 2) {
					argsValue = splitted[1];
				}
				arguments.put(argsName,argsValue);
			}
		}
		return arguments;
	}
}
