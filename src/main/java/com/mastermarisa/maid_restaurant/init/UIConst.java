package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.client.gui.element.ImageData;

import java.awt.*;

public interface UIConst {
    Color fullBlack = Color.BLACK;
    Color lessBlack = new Color(0, 0, 0, 128);
    Color leastBlack = new Color(0, 0, 0, 64);
    ImageData bookImage = new ImageData(MaidRestaurant.resourceLocation("textures/gui/food_book.png"),new Rectangle(1,1,148,180),148,180,161,256);
    ImageData blackListImage = new ImageData(MaidRestaurant.resourceLocation("textures/gui/food_book.png"),new Rectangle(32,224,16,16),16,16,161,256);
    ImageData whiteListImage = new ImageData(MaidRestaurant.resourceLocation("textures/gui/food_book.png"),new Rectangle(48,224,16,16),16,16,161,256);
    ImageData arrowBrightImage = new ImageData(MaidRestaurant.resourceLocation("textures/gui/arrow_bright.png"),new Rectangle(0,0,8,9),8,9,8,9);
    ImageData orderedTagImage = new ImageData(MaidRestaurant.resourceLocation("textures/gui/order_tag.png"),new Rectangle(0,0,58,18),58,18,58,18);
    ImageData confirmTagImage = new ImageData(MaidRestaurant.resourceLocation("textures/gui/confirm_tag.png"),new Rectangle(0,0,24,18),24,18,24,18);
    ImageData bandImage = new ImageData(MaidRestaurant.resourceLocation("textures/gui/request_queue.png"),new Rectangle(116,272,184,17),184,17,399,419);
    ImageData band_1 = new ImageData(MaidRestaurant.resourceLocation("textures/gui/band_2.png"),new Rectangle(0,0,138,17),138,17,138,17);
    ImageData typeBubble = new ImageData(MaidRestaurant.resourceLocation("textures/gui/type_bubble.png"),new Rectangle(0,0,18,22),18,22,18,22);
    ImageData requestImage = new ImageData(MaidRestaurant.resourceLocation("textures/gui/request_queue.png"),new Rectangle(19,275,18,70),18,70,399,419);
    ImageData basket_1 = new ImageData(MaidRestaurant.resourceLocation("textures/gui/basket_1.png"),new Rectangle(0,0,32,33),32,33,32,33);
    ImageData basket_2 = new ImageData(MaidRestaurant.resourceLocation("textures/gui/basket_2.png"),new Rectangle(0,0,32,33),32,33,32,33);
    ImageData basket_3 = new ImageData(MaidRestaurant.resourceLocation("textures/gui/basket_3.png"),new Rectangle(0,0,32,37),32,37,32,37);
    ImageData bubble = new ImageData(MaidRestaurant.resourceLocation("textures/gui/bubble_2.png"),new Rectangle(0,0,11,14),11,14,11,14);
}
