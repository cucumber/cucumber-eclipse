#!/usr/bin/env python3
"""
Behave formatter plugin that sends Cucumber messages to Eclipse IDE.

This formatter connects to Eclipse via a socket connection and sends
test execution messages using the Cucumber Messages protocol.

The formatter uses plain JSON for message serialization to avoid requiring
additional dependencies. For full Cucumber Messages support, consider using
the cucumber-messages Python library.
"""

import os
import sys
import socket
import struct
import json
from behave.model import Feature, Scenario, Step
from behave.formatter.base import Formatter


class CucumberEclipseFormatter(Formatter):
    """
    Behave formatter that sends Cucumber messages to Eclipse via socket.
    
    This formatter implements the same protocol as the Java CucumberEclipsePlugin,
    allowing Eclipse to receive real-time test execution updates.
    
    Protocol:
    1. Connect to Eclipse on specified port
    2. For each message:
       - Serialize message as JSON (Cucumber Message format)
       - Send 4-byte integer (big-endian) with message length
       - Send JSON message bytes
       - Wait for acknowledgment byte (0x01)
    3. After TestRunFinished, send 0 length and wait for goodbye (0x00)
    
    The formatter sends simplified Cucumber Messages that are compatible with
    the Eclipse unittest view. Full Cucumber Messages support can be added by
    using the cucumber-messages Python library.
    """
    
    HANDLED_MESSAGE = 0x01
    GOOD_BY_MESSAGE = 0x00
    
    def __init__(self, stream_opener, config):
        super(CucumberEclipseFormatter, self).__init__(stream_opener, config)
        
        # Get port from environment variable or userdata
        port = os.environ.get('CUCUMBER_ECLIPSE_PORT')
        if not port:
            port = config.userdata.get('cucumber_eclipse_port')
        
        if not port:
            raise ValueError(
                "Port not specified. Set CUCUMBER_ECLIPSE_PORT environment variable "
                "or pass -D cucumber_eclipse_port=<port>"
            )
        
        try:
            self.port = int(port)
        except ValueError:
            raise ValueError(f"Invalid port number: {port}")
        
        # Connect to Eclipse
        self.socket = None
        self.connected = False
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.connect(('localhost', self.port))
            self.connected = True
            print(f"Connected to Eclipse on port {self.port}", file=sys.stderr)
        except Exception as e:
            print(f"Failed to connect to Eclipse on port {self.port}: {e}", file=sys.stderr)
            self.socket = None
            self.connected = False
        
        # Track test state
        self.feature_count = 0
        self.scenario_count = 0
        self.step_count = 0
    
    def __del__(self):
        """Cleanup on destruction"""
        self.close()
    
    def close(self):
        """Close the socket connection"""
        if self.socket and self.connected:
            try:
                # Send final goodbye message
                self._send_message_length(0)
                self._read_acknowledgment()
                self.socket.close()
            except:
                pass
            finally:
                self.socket = None
                self.connected = False
    
    def _send_message(self, envelope):
        """
        Send a Cucumber message envelope to Eclipse.
        
        Args:
            envelope: Dictionary containing the Cucumber message
        """
        if not self.connected or not self.socket:
            return
        
        try:
            # Serialize to JSON
            message_json = json.dumps(envelope, separators=(',', ':'))
            message_bytes = message_json.encode('utf-8')
            
            # Send message length (4-byte big-endian integer)
            self._send_message_length(len(message_bytes))
            
            # Send message
            self.socket.sendall(message_bytes)
            
            # Wait for acknowledgment
            ack = self._read_acknowledgment()
            
            # Check if Eclipse sent goodbye
            if ack == self.GOOD_BY_MESSAGE:
                self.connected = False
                
        except Exception as e:
            print(f"Error sending message: {e}", file=sys.stderr)
            self.connected = False
    
    def _send_message_length(self, length):
        """Send message length as 4-byte big-endian integer"""
        length_bytes = struct.pack('>I', length)
        self.socket.sendall(length_bytes)
    
    def _read_acknowledgment(self):
        """Read single acknowledgment byte from Eclipse"""
        ack_byte = self.socket.recv(1)
        if ack_byte:
            return ack_byte[0]
        return 0
    
    # Behave lifecycle methods
    
    def feature(self, feature):
        """Called when a feature is about to be executed"""
        self.feature_count += 1
        # Send TestCaseStarted equivalent
        # Note: Behave doesn't have the same granular events as Cucumber
        # We'll send simplified messages
    
    def scenario(self, scenario):
        """Called when a scenario is about to be executed"""
        self.scenario_count += 1
    
    def step(self, step):
        """Called after a step has been executed"""
        self.step_count += 1
        
        # Create a simplified TestStepFinished message
        # The actual Cucumber Messages schema is complex, so we create a minimal version
        envelope = {
            "testStepFinished": {
                "testStepId": f"step-{self.step_count}",
                "testStepResult": {
                    "status": self._get_step_status(step),
                    "duration": getattr(step, 'duration', 0) * 1000000000  # Convert to nanoseconds
                }
            }
        }
        
        self._send_message(envelope)
    
    def _get_step_status(self, step):
        """Convert Behave step status to Cucumber status"""
        status_map = {
            'passed': 'PASSED',
            'failed': 'FAILED',
            'skipped': 'SKIPPED',
            'undefined': 'UNDEFINED',
            'untested': 'PENDING'
        }
        return status_map.get(str(step.status), 'UNKNOWN')
    
    def eof(self):
        """Called at the end of the test run"""
        # Send TestRunFinished message
        envelope = {
            "testRunFinished": {
                "success": True,
                "timestamp": {
                    "seconds": 0,
                    "nanos": 0
                }
            }
        }
        self._send_message(envelope)
        self.close()


# Register the formatter with Behave
def register_formatter():
    """
    This function is called by Behave to register custom formatters.
    
    Add to behave.ini:
    [behave.formatters]
    cucumber-eclipse = behave_cucumber_eclipse:CucumberEclipseFormatter
    """
    return CucumberEclipseFormatter
