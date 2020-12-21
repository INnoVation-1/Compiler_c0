package miniplc0java.util;

public class DoubleLiteral {
    public StringBuilder integerPart = new StringBuilder();
    public StringBuilder floatPart = new StringBuilder();
    public boolean isNegate = false;

    public DoubleLiteral(String integerPart, String floatPart, String ePart) {
        int eNum = Integer.parseInt(ePart);
        int i;
        if(eNum > 0){
            if(eNum >= floatPart.length()){
                this.integerPart.append(integerPart);
                this.integerPart.append(floatPart);
                for(i = 0; i < eNum - floatPart.length(); i++){
                    this.integerPart.append("0");
                }
                this.floatPart.append("0");
            }else{
                this.integerPart.append(integerPart);
                this.integerPart.append(floatPart, 0, eNum);
                this.floatPart.append(floatPart.substring(eNum));
            }
        }else if(eNum < 0){
            eNum *= -1;
            if(eNum >= integerPart.length()){
                for(i = 0; i < eNum - integerPart.length(); i++){
                    this.floatPart.append("0");
                }
                this.floatPart.append(integerPart);
                this.floatPart.append(floatPart);
                this.integerPart.append("0");
            }else{
                this.floatPart.append(integerPart.substring(integerPart.length() - eNum));
                this.floatPart.append(floatPart);
                this.integerPart.append(integerPart, 0, integerPart.length() - eNum);
            }
        }else {
            this.integerPart.append(integerPart);
            this.floatPart.append(floatPart);
        }
    }

    public DoubleLiteral(String integerPart, String floatPart) {
        this.integerPart.append(integerPart);
        this.floatPart.append(floatPart);
    }

    public StringBuilder int2Sb(StringBuilder sb){
        StringBuilder temp = new StringBuilder();
        StringBuilder div = new StringBuilder();
        StringBuilder mod = new StringBuilder();
        if(sb.equals("0")){
            return temp;
        }
        int ta;
        for(int i = 0; i < sb.length(); i++){
            ta = (int)sb.charAt(i) - (int)'0';
            if(ta == 1){
                if(i == 0){
                    if(i == sb.length()){
                        div.append("0");
                        return div;
                    }
                }else{
                    div.append("0");
                }
                i++;
                ta = 10 + (int)sb.charAt(i) - (int)'0';
            }
            div.append(ta / 2);
        }

        return int2Sb(div);
    }

    public String generateBinaryValue(){
        StringBuilder sbBinary = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        if(isNegate){
            sbBinary.append(1);
        }else{
            sbBinary.append(0);
        }

        //Double.




        return sbBinary.toString();
    }

    @Override
    public String toString(){
        return integerPart + "." + floatPart;
    }

    public static void main(String[] args){
        System.out.println(new DoubleLiteral("12321", "3123", "-1"));
    }
}
