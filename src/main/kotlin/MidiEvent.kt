interface IEvent {
	var eMidiType:TrackEventType//{get}
	var timeDelta:UInt//{get}
}

data class MidiChannelEvent(override var eMidiType:TrackEventType,
                            override var timeDelta:UInt,
                            var eventTimeDelta:UInt,
                            var channel:UByte?,
                            var pressure:UByte?,
                            var noteVelocity:UByte?,
                            var programNumber:UByte?,
                            var musicalNote:MidiNote?,
                            var controllerNumber:UByte?,
                            var controllerChangeValue:UByte?,

									 //This is actually a 14 bit value
									 var pitchWheelChange:UShort?,
									 var eventType:MidiMajorMessage
									) : IEvent{}