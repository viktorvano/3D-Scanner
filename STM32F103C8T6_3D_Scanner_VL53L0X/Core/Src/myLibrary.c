/*
 * myLibrary.c
 *
 *  Created on: 11. 2. 2021
 *      Author: vikto
 */

#include "myLibrary.h"

uint16_t pitch = 0, yaw = 90;
uint8_t measureFlag = 0, homingCounter = 0;

void setPitch(uint16_t degrees)
{
	if(degrees > 90)
		degrees = 90;
	if(degrees < 0)
		degrees = 0;
	uint16_t pitch = ((float)(pitchMax-pitchMin)/90.0f)*degrees + pitchMin;
	__HAL_TIM_SetCompare(&htim2, TIM_CHANNEL_1, pitch);
}

void setYaw(uint16_t degrees)
{
	if(degrees > 180)
		degrees = 180;
	if(degrees < 0)
		degrees = 0;
	uint16_t yaw = ((float)(yawMax-yawMin)/180.0f)*degrees + yawMin;
	__HAL_TIM_SetCompare(&htim2, TIM_CHANNEL_2, yaw);
}

uint16_t measureDistanceAt(uint16_t pitch, uint16_t yaw)
{
	setPitch(pitch);
	setYaw(yaw);
	if(pitch==0 && yaw==0)
		HAL_Delay(2000);
	else
		HAL_Delay(50);

	return (uint16_t)tofReadDistance();
}
