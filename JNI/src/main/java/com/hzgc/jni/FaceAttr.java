package com.hzgc.jni;

import java.io.Serializable;

public class FaceAttr implements Serializable{
    public float[] feature;

    public float[]getFeature(){return feature;}
    public void setFeature(float[] feature){this.feature=feature;}

    /**
     * 头发颜色：0->无；1->金色；2->黑色；3->棕色；4->灰白
     */
    public enum HairColor{

        None(0),Blond(1),Black(2),Brown(3),Gray(4);

        private int haircolorvalue;

        private HairColor(int haircolorvalue){ this.haircolorvalue=haircolorvalue;}
        public int getHaircolor() { return haircolorvalue; }
        public void setHaircolor(int haircolorvalue) { this.haircolorvalue = haircolorvalue;}

        public String toString() {
            return "HairColor{" +"color=" +haircolorvalue+'}';
        }
    }

    /**
     * 头发类型：0->无；1->直发；2->卷发；3->光头；
     */
    public enum HairStyle{

        None(0),Straight(1),Wavy(2),Bald(3);

        private int hairstylevalue;

        private HairStyle(int hairstyle){this.hairstylevalue=hairstylevalue; }
        public int getHairstyle() { return hairstylevalue;}
        public void setHairstyle(int hairstylevalue) { this.hairstylevalue = hairstylevalue;}

        public String toString() {
            return "HairStyle{" +"style=" +hairstylevalue+'}';
        }
    }

    /**
     * 性别：0->无；1->男；2->女；
     */
    public enum Gender{

        None(0),Male(1),Female(2);

        private int gendervalue;

        private Gender(int gendervalue){ this.gendervalue=gendervalue;}
        public int getGendervalue() { return gendervalue; }
        public void setGendervalue(int gendervalue) { this.gendervalue = gendervalue;}

        public String toString() {
            return "Gendervalue{" +"value=" +gendervalue+'}';
        }
    }

    /**
     * 是否带帽子：0->无；1->戴帽子；2->没有戴帽子；
     */
    public enum Hat{

        None(0),Hat_y(1),Hat_n(2);

        private int hatvalue;

        private Hat(int hatvalue){ this.hatvalue=hatvalue;}
        public int getHatvalue() { return hatvalue; }
        public void setHatvalue(int hatvalue) { this.hatvalue = hatvalue;}

        public String toString() {
            return "HatValue{" +"value=" +hatvalue+'}';
        }

    }

    /**
     * 是否系领带：0->无；1->系领带；2->没有系领带；
     */
    public enum Tie{

        None(0),Tie_y(1),Tie_n(2);

        private int tievalue;

        private Tie(int tievalue){ this.tievalue=tievalue;}
        public int getTievalue() { return tievalue; }
        public void setTievalue(int tievalue) { this.tievalue = tievalue;}

        public String toString() {
            return "TieValue{" +"value=" +tievalue+'}';
        }
    }

    /**
     * 胡子类型：0->无；1->鼻子和嘴唇之间的胡子;2->山羊胡；3->络腮胡；4->没有胡子；
     */
    public enum Huzi{

        None(0),Mustache(1),Goatee(2),Sideburns(3),Nobeard(4);

        private int huzivalue;

        private Huzi(int huzivalue){ this.huzivalue=huzivalue;}
        public int getHuzivalue() { return huzivalue; }
        public void setHuzivalue(int huzivalue) { this.huzivalue = huzivalue;}

        public String toString() {
            return "HuziValue{" +"value=" +huzivalue+'}';
        }
    }

    /**
     * 是否戴眼镜：0->无；1->戴眼镜；2->没有戴眼镜；
     */
    public enum Eyeglasses{

        None(0),Eyeglasses_y(1),Eyeglasses_n(2);

        private int eyeglassesvalue;

        private Eyeglasses(int eyeglassesvalue){ this.eyeglassesvalue=eyeglassesvalue;}
        public int getEyeglassesvalue() { return eyeglassesvalue; }
        public void setEyeglassesvalue(int eyeglassesvalue) { this.eyeglassesvalue = eyeglassesvalue;}

        public String toString() {
            return "EyeglassesValue{" +"value=" +eyeglassesvalue+'}';
        }
    }

}
