package miniplc0java.Analyser;

import java.util.ArrayList;

public class ReturnCheck {
    ArrayList<ReturnPoint>returnPoints=new ArrayList<>();
    public ReturnPoint top(){
        return returnPoints.get(returnPoints.size()-1);
    }
    public ReturnPoint preTop(){
        return returnPoints.get(returnPoints.size()-2);
    }
    public void getResult(){
        while (true){
            if(top().returnEnum==ReturnEnum.FUNCTION)break;
            else if(top().returnEnum==ReturnEnum.IF){
                returnPoints.remove(returnPoints.size()-1);
                break;
            }else if(top().returnEnum==ReturnEnum.ELSE&&preTop().returnEnum==ReturnEnum.ELSE_IF){
                if(!top().ifReturn||!preTop().ifReturn)
                    top().ifReturn=false;
                returnPoints.remove(returnPoints.size()-2);
            }else if(top().returnEnum==ReturnEnum.ELSE&&preTop().returnEnum==ReturnEnum.IF){
//                if(returnPoints.get(returnPoints.size()-3).returnEnum!=ReturnEnum.FUNCTION)
//                    throw new Error("123");
                if(top().ifReturn&&preTop().ifReturn)
                    returnPoints.get(returnPoints.size()-3).ifReturn=true;
                returnPoints.remove(returnPoints.size()-1);
                returnPoints.remove(returnPoints.size()-1);
                break;
            }else if(top().returnEnum==ReturnEnum.ELSE_IF&&preTop().returnEnum==ReturnEnum.IF){
//                if(returnPoints.get(returnPoints.size()-3).returnEnum!=ReturnEnum.FUNCTION)
//                    throw new Error("123");
                returnPoints.remove(returnPoints.size()-1);
                returnPoints.remove(returnPoints.size()-1);
                break;
            }else if(top().returnEnum==ReturnEnum.ELSE_IF&&preTop().returnEnum==ReturnEnum.ELSE_IF){
                returnPoints.remove(returnPoints.size()-1);
                returnPoints.remove(returnPoints.size()-1);
            }else if(top().returnEnum==ReturnEnum.IF&&preTop().returnEnum==ReturnEnum.FUNCTION){
                returnPoints.remove(returnPoints.size()-1);
            }
        }
    }
}
