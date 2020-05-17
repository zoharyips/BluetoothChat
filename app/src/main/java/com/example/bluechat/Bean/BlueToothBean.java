package com.example.bluechat.Bean;


import com.orm.SugarRecord;

import java.util.List;

public class BlueToothBean extends SugarRecord {
    private String name;
    private String mac;
    Integer score;
    Integer count;
    Integer last;

    public BlueToothBean(){};

    public BlueToothBean(String name, String mac) {
        this.name = name;
        this.mac = mac;
        this.score = -1;
        this.last = -1;
        this.count = 0;
    }

    public Integer getLast() {
        return last;
    }

    public void setLast(Integer last) {
        this.last = last;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void AddCount()
    {
        this.count = this.count+1;
    }

    public void CalScore()
    {
        if (score<0)
        {
            score = 60;
            return;
        }
        last = score;

        List<BlueToothBean> tmp = BlueToothBean.find(BlueToothBean.class,"score > ?","0");
        if (tmp.isEmpty())
        {
            return;
        }

        float m = 0.7f, n = 0.3f;
        float r = 0.6f, q = 0.4f;
        float a = 0.7f, b = 0.3f;
        float t,th,tf,ts;
        float cTotal=0, dTotal=0;
        int cNum=0, dNum=0;
        int num1 = 0,num2 = count;
        for (BlueToothBean blue:tmp)
        {
            if (!blue.getMac().equals(mac))
            {
                num1+=blue.getCount();
            }

            if (blue.getScore()>=70)
            {
                cTotal+=blue.getScore();
                cNum++;
            }
            else
            {
                dTotal+=blue.getScore();
                dNum++;
            }
        }
        if (cNum==0)
        {
            cTotal = 60;
            cNum = 1;//防止除以0
        }
        if (dNum==0)
        {
            dTotal = 60;
            dNum = 1;//防止除以0
        }

        //自身
        ts = 60+num2-num1;

        //历史
        if (last>0)
        {
            th = (ts+last)*0.5f;
        }
        else
        {
            th = ts;
        }

        //朋友
        tf = a*(cTotal/cNum)+b*(dTotal/dNum);

        t = r*(m*th+n*tf)+q*ts;

        score = (int)t;

    }
}
