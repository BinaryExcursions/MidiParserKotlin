interface IEvent {
	var midiEventIdentifier:MidiMajorMessage
	var eMidiType:TrackEventType//{get}
	var channel:UByte?
	var timeDelta:UInt//{get}

}

data class MidiNoteOnEvent(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.NOTE_ON,
									override var eMidiType:TrackEventType = TrackEventType.MIDI_EVENT,
                           override var timeDelta:UInt,
                           override var channel: UByte?,
                           var musicalNote:MidiNote,
                           var noteVelocity:UByte
) : IEvent{}

data class MidiNoteOffEvent(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.NOTE_OFF,
                            override var eMidiType:TrackEventType = TrackEventType.MIDI_EVENT,
                            override var timeDelta:UInt,
                            override var channel: UByte?,
                            var musicalNote:MidiNote,
                            var noteVelocity:UByte
) : IEvent{}

// Polyphonic Key Pressure (Aftertouch).
data class MidiPolyphonicKeyPressureEvent(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.KEY_PRESSURE_AFTER_TOUCH,
                                          override var eMidiType:TrackEventType = TrackEventType.MIDI_EVENT,
                                          override var timeDelta:UInt,
                                          override var channel: UByte?,
                                          var musicalNote:MidiNote,
                                          var pressure:UByte
) : IEvent{}

data class MidiControlChangeEvent(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.CONTROL_CHANGE,
                                  override var eMidiType:TrackEventType = TrackEventType.MIDI_EVENT,
                                  override var timeDelta:UInt,
                                  override var channel: UByte?,
                                  var controllerNumber:UByte,
                                  var controllerChangeValue:UByte,
) : IEvent{}

data class MidiProgramChangeEvent(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.PROGRAM_CHANGE,
                                  override var eMidiType:TrackEventType = TrackEventType.MIDI_EVENT,
                                  override var timeDelta:UInt,
                                  override var channel: UByte?,
                                  var programNumber:UByte
) : IEvent{}

//Channel Pressure (After-touch).
data class MidiChannelPressureEvent(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.CHANNEL_PRESSURE_AFTER_TOUCH,
                                    override var eMidiType:TrackEventType = TrackEventType.MIDI_EVENT,
                                    override var timeDelta:UInt,
                                    override var channel: UByte?,
                                    var pressure:UByte
) : IEvent{}

/**
 * 1110nnnn
	Pitch Wheel Change.
	This message is sent to indicate a change in the pitch wheel. The pitch wheel is measured by a
	fourteen bit value. Centre (no pitch change) is 2000H. Sensitivity is a function of the transmitter.

	0lllllll
	0mmmmmmm

	(lllllll) are the least significant 7 bits.
	(mmmmmmm) are the most significant 7 bits.
*/
data class MidiPitchWheelEvent(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.PITCH_WHEEL_CHANGE,
                               override var eMidiType:TrackEventType = TrackEventType.MIDI_EVENT,
                               override var timeDelta:UInt,
                               override var channel: UByte?,
										 //This is actually a 14 bit value
	                            var pitchWheelChange:UShort,
) : IEvent{}

//data class MidiChannelEvent(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.NOTE_ON,
//                            override var eMidiType:TrackEventType = TrackEventType.MIDI_EVENT,
//                            override var timeDelta:UInt,
//                            override var channel: UByte?,
//                            var eventTimeDelta:UInt,
//                            //var channel:UByte?,
//                            var pressure:UByte?,
//                            var noteVelocity:UByte?,
//                            var programNumber:UByte?,
//                            var musicalNote:MidiNote?,
//                            var controllerNumber:UByte?,
//                            var controllerChangeValue:UByte?,
//
//									 //This is actually a 14 bit value
//									 var pitchWheelChange:UShort?,
//									 var eventType:MidiMajorMessage
//									) : IEvent{}

data class MetaEventSequenceNumber(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.UNDEFINED,
                                    override var eMidiType:TrackEventType = TrackEventType.META_EVENT,
                                    override var channel: UByte? = null,
                                    override var timeDelta:UInt = 0x0u, //This should always be zero to meta events - if it isn't something is wrong.
                                    var sequenceNumber:UShort? = null,
                                    var metaEventType:MetaEventDefinitions):IEvent

data class MetaEventText(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.UNDEFINED,
                         override var eMidiType:TrackEventType = TrackEventType.META_EVENT,
                         override var channel: UByte? = null,
                         override var timeDelta:UInt = 0x0u, //This should always be zero to meta events - if it isn't something is wrong.
                         var textInfo:String,
                         var metaEventType:MetaEventDefinitions):IEvent

data class MetaEventChannel(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.UNDEFINED,
                              override var eMidiType:TrackEventType = TrackEventType.META_EVENT,
                              override var channel: UByte?, //Between 0 - 15, both numbers inclusive
                              override var timeDelta:UInt = 0x0u, //This should always be zero to meta events - if it isn't something is wrong.
                              var metaEventType:MetaEventDefinitions):IEvent

data class MetaEventSetTempo(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.UNDEFINED,
                              override var eMidiType:TrackEventType = TrackEventType.META_EVENT,
                              override var channel: UByte? = null,
                              override var timeDelta:UInt = 0x0u, //This should always be zero to meta events - if it isn't something is wrong.
                              var tempo:UInt,
                              var metaEventType:MetaEventDefinitions):IEvent

data class MetaEventSMPTEOffset(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.UNDEFINED,
                                override var eMidiType:TrackEventType = TrackEventType.META_EVENT,
                                override var channel: UByte? = null,
                                override var timeDelta:UInt = 0x0u, //This should always be zero to meta events - if it isn't something is wrong.
                                var hour:UByte,
                                var minute:UByte,
                                var seconds:UByte,
                                var millisec:UByte,
                                var fractionalFrames:UByte,
                                var metaEventType:MetaEventDefinitions):IEvent

data class MetaEventTimeSignature(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.UNDEFINED,
                                  override var eMidiType:TrackEventType = TrackEventType.META_EVENT,
                                  override var channel: UByte? = null,
                                  override var timeDelta:UInt = 0x0u, //This should always be zero to meta events - if it isn't something is wrong.
                                  var timeSignature:TimingInfo,
                                  var clockClicks:UByte,
                                  var numberOf32ndNotes:UByte,
                                  var metaEventType:MetaEventDefinitions):IEvent

data class MetaEventKeySignature(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.UNDEFINED,
                                  override var eMidiType:TrackEventType = TrackEventType.META_EVENT,
                                  override var channel: UByte? = null,
                                  override var timeDelta:UInt = 0x0u, //This should always be zero to meta events - if it isn't something is wrong.
                                  var keySignature:MusicalKey,
                                  var metaEventType:MetaEventDefinitions):IEvent

data class MetaEventOutputPort(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.UNDEFINED,
                                 override var eMidiType:TrackEventType = TrackEventType.META_EVENT,
                                 override var channel: UByte? = null,
                                 override var timeDelta:UInt = 0x0u, //This should always be zero to meta events - if it isn't something is wrong.
                                 var PortNumber:UByte,
                                 var metaEventType:MetaEventDefinitions):IEvent

data class SysExclusionEvent(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.UNDEFINED,
                              override var eMidiType:TrackEventType = TrackEventType.SYSEX_EVENT,
                              override var timeDelta:UInt,
                              override var channel: UByte?):IEvent

data class SysRealtimeEvent(override var midiEventIdentifier:MidiMajorMessage = MidiMajorMessage.UNDEFINED,
                             override var eMidiType:TrackEventType = TrackEventType.SYSREALTIME_EVENT,
                             override var timeDelta:UInt,
                             override var channel: UByte?):IEvent