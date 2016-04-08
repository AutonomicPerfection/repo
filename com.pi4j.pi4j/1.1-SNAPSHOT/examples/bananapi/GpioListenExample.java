package bananapi;
/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Examples
 * FILENAME      :  GpioListenExample.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2016 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.platform.Platform;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.platform.PlatformManager;
import com.pi4j.util.CommandArgumentParser;

/**
 * This example code demonstrates how to setup a listener
 * for GPIO pin state changes on the BananaPi.
 *
 * The internal resistance is set to PULL UP by default. So when
 * connecting your GPIO pin to a ground pin, you should see the
 * GpioPinListenerDigital fire the event.
 *
 * @author Robert Savage
 */
public class GpioListenExample {

    /**
     * [ARGUMENT/OPTION "--pin (#)" | "-p (#)" ]
     * This example program accepts an optional argument for specifying the GPIO pin (by number)
     * to use with this GPIO listener example. If no argument is provided, then GPIO #1 will be used.
     * -- EXAMPLE: "--pin 4" or "-p 0".
     *
     * [ARGUMENT/OPTION "--pull (up|down)" | "-u (up|down)" | "--up" | "--down" ]
     * This example program accepts an optional argument for specifying pin pull resistance.
     * Supported values: "up|down" (or simply "1|0").   If no value is specified in the command
     * argument, then the pin pull resistance will be set to PULL_UP by default.
     * -- EXAMPLES: "--pull up", "-pull down", "--up", "--down", "-pull 0", "--pull 1", "-u up", "-u down.
     *
     * @param args
     * @throws InterruptedException
     * @throws PlatformAlreadyAssignedException
     */
    public static void main(String args[]) throws InterruptedException, PlatformAlreadyAssignedException {
        System.out.println("<--Pi4J--> GPIO Listen Example ... started.");

        // ####################################################################
        //
        // since we are not using the default Raspberry Pi platform, we should
        // explicitly assign the platform as the BananaPi platform.
        //
        // ####################################################################
        PlatformManager.setPlatform(Platform.BANANAPI);

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // ####################################################################
        //
        // When provisioning a pin, use the BananaPiPin class.
        //
        // Please note that not all GPIO pins support edge triggered interrupt
        // and thus not all pins are eligible for pin state change listeners.
        // These pins must be polled to detect state changes.  See the
        // 'GpioListenAllExample' for a complete example including polled
        // pins.
        //
        // ####################################################################

        // by default we will use gpio pin #01; however, if an argument
        // has been provided, then lookup the pin by address
        Pin pin = CommandArgumentParser.getPin(
                BananaPiPin.class,    // pin provider class to obtain pin instance from
                BananaPiPin.GPIO_01,  // default pin if no pin argument found
                args);                // argument array to search in

        // by default we will use gpio pin PULL-UP; however, if an argument
        // has been provided, then use the specified pull resistance
        PinPullResistance pull = CommandArgumentParser.getPinPullResistance(
                PinPullResistance.PULL_UP,  // default pin pull resistance if no pull argument found
                args);                      // argument array to search in

        // provision gpio pin as an input pin with its internal pull up resistor set
        final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(pin, pull);

        // unexport the GPIO pin when program exits
        myButton.setShutdownOptions(true);

        System.out.println("Successfully provisioned [" + pin + "] with PULL resistance = [" + pull + "]");

        // create and register gpio pin listener
        myButton.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
            }

        });

        System.out.println(" ... complete the [" + pin + "] circuit and see the listener feedback here in the console.");
        System.out.println(" ... press the [ENTER] key to exit");

        // keep program running until user exits
        System.console().readLine();

        // forcefully shutdown all GPIO monitoring threads and scheduled tasks
        gpio.shutdown();  // <--- implement this method call if you wish to terminate the Pi4J GPIO controller
    }
}
