/*
 * myLibrary.h
 *
 *  Created on: 11. 2. 2021
 *      Author: vikto
 */

#ifndef INC_MYLIBRARY_H_
#define INC_MYLIBRARY_H_

#define pitchMin	260
#define pitchMax	431
#define yawMin		98
#define yawMax		455

#include "usbd_cdc_if.h"
#include "main.h"
#include "tof.h"

extern TIM_HandleTypeDef htim2;
extern TIM_HandleTypeDef htim3;

extern uint16_t pitch, yaw;
extern uint8_t measureFlag, homingCounter;

void setPitch(uint16_t degrees);
void setYaw(uint16_t degrees);
uint16_t measureDistanceAt(uint16_t pitch, uint16_t yaw);

#endif /* INC_MYLIBRARY_H_ */
