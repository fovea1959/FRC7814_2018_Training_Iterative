/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team7814.robot;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Relay.Value;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends TimedRobot {
	
	// declare WPILib hardware
	static public DifferentialDrive drive;
	static public Talon leftShooterController, rightShooterController;
	static public Relay pickupRelay;
	static public DigitalInput leftSideShooterSensor, rightSideShooterSensor, ballSensor;
	static public AnalogInput aio0;
	
	// declare some variables for joystick
	static public Joystick driverJoystick;
	static public JoystickButton shooterButton, pickupButton;
	
	// here are the variables we need to keep track of the joystick buttons
	boolean lastShooterButton;
	// here are the variables we need to keep track of the pickup
	boolean shooterIsOn;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		// set up the robot drive
		Talon leftDriveController = new Talon(0);
		leftDriveController.setName("Drive", "Left Drive Motor");
		LiveWindow.add(leftShooterController);
		Talon rightDriveController = new Talon(1);
		rightDriveController.setName("Drive", "Right Drive Motor");
		LiveWindow.add(rightShooterController);
		drive = new DifferentialDrive(leftDriveController, rightDriveController);
		drive.setName("Drive", "Differential Drive");
		// LiveWindow.add(drive);

		// set up the shooter controllers
		leftShooterController = new Talon(4);
		leftShooterController.setName("Shooter", "Left Shooter Motor");
		LiveWindow.add(leftShooterController);
		rightShooterController = new Talon(5);
		rightShooterController.setName("Shooter", "Right Shooter Motor");
		LiveWindow.add(rightShooterController);
		
		// set up the pickup relay
		pickupRelay = new Relay(0, Relay.Direction.kBoth);
		pickupRelay.setName("Shooter", "Pickup Motor");
		LiveWindow.add(pickupRelay);
		
		// digital inputs
		leftSideShooterSensor = new DigitalInput(1);
		leftSideShooterSensor.setName("Shooter", "Left Side Shooter Sensor");
		LiveWindow.add(leftSideShooterSensor);
		rightSideShooterSensor = new DigitalInput(2);
		rightSideShooterSensor.setName("Shooter", "Right Side Shooter Sensor");
		LiveWindow.add(rightSideShooterSensor);
		ballSensor = new DigitalInput(3);
		ballSensor.setName("Shooter", "Ball Sensor");
		LiveWindow.add(ballSensor);
		
		// analog inputs
		aio0 = new AnalogInput(0);
		aio0.setName("Drive", "aio0");
		LiveWindow.add(aio0);
		
		// set up the Joystick stuff
		driverJoystick = new Joystick(0);
		pickupButton = new JoystickButton(driverJoystick, 1);
		shooterButton = new JoystickButton(driverJoystick, 3);
	}

	@Override
	public void autonomousInit() {
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		putSensorValuesOnDashboard();
	}

	@Override
	public void teleopInit() {
		// reset everything to known state
		lastShooterButton = false;
		shooterIsOn = false;
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		/* 
		 * drive the robot
		 */
		double joyY = driverJoystick.getY();
		double joyX = driverJoystick.getX();
		// we need to say -joyY because the joystick registers negative when
		// pushed forward, but arcadeDrive wants a positive number to move forward
		drive.arcadeDrive(-joyY, joyX);
		SmartDashboard.putNumber("joystick X", joyX);
		SmartDashboard.putNumber("joystick Y", joyY);
		
		/*
		 * handle the shooter button. if it's down and didn't used to be down,
		 * then it's freshly depressed ('tripped'). If it's tripped, then toggle the
		 * pickup motors (turn them off if they were on, and turn them on if they were off).
		 */
		// read the button
		boolean shooterButtonIsDown = shooterButton.get();
		
		// figure out if we are tripped
		boolean shooterButtonIsTripped = false;
		if (lastShooterButton == false && shooterButtonIsDown == true) {
			// the button was not down before before, but is now
			shooterButtonIsTripped = true;
		}
		
		// remember for next time
		lastShooterButton = shooterButtonIsDown;
		
		// if we are tripped, figure out what to do with the shooter. Turn it off 
		// if it was on, turn it on if it was off.
		if (shooterButtonIsTripped) {
			shooterIsOn = ! shooterIsOn;
		}
		
		// ...and turn the motors on or off...
		setShooters(shooterIsOn);
		
		/*
		 * handle the pickup button. only run the pickups when the button is down
		 */
		if (pickupButton.get()) {
			setPickup(true);
		} else {
			setPickup(false);
		}
		
		/*
		 * let the drivers see what the sensors have
		 */
		putSensorValuesOnDashboard();
	}

	@Override
	public void testInit() {
		super.testInit();
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}

	@Override
	public void disabledInit() {
	}

	@Override
	public void disabledPeriodic() {
		putSensorValuesOnDashboard();
	}
	
	/*
	 * turn the shooter motors on or off
	 */
	void setShooters(boolean on) {
		double power = 0;
		if (on) {
			power = 0.25;
		}
		leftShooterController.set(power);
		rightShooterController.set(power);
		
		/*
		 * update the dashboard with some sensor values
		 */
		putSensorValuesOnDashboard();
	}
	
	/*
	 * update the dashboard with sensor values
	 */
	void putSensorValuesOnDashboard() {
		SmartDashboard.putBoolean("dio1", leftSideShooterSensor.get());
		SmartDashboard.putBoolean("dio2", rightSideShooterSensor.get());
		SmartDashboard.putNumber("aio0",  aio0.getVoltage());
	}
	
	/*
	 * turn the pickup motor on or off
	 */
	void setPickup (boolean on) {
		if (on) {
			pickupRelay.set(Value.kReverse);
		} else {
			pickupRelay.set(Value.kOff);
		}
	}
}
