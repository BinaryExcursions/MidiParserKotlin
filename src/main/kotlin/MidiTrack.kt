class MidiTrack
{
	private var m_TrackBlockTitle:UInt = 0u
	private lateinit var m_Events:ArrayList<IEvent>
	private var m_TrackIndex:Int = -1 //Will this be 0 based????? To be determined

	//--Holders until I know more about these variables
	private var m_ChunkType:Int = 0 //I think this is actually the MTrk
	private var m_ChunkLength:Int = 0 //I think this is the nummber of bytes in the record chunk
	//--End holders segment

	//The first track of type 1 file will need to contain tempo information
	//Remember, 4/4 at 120 bpm is always assumed if not present.
	private var m_TempoMapInfo:Pair<TimingInfo, UByte>? = null// = (.FOUR_FOUR, 120) //Here for defaults, but still ONLY in the first track unless it changes at some point

	var TrackBlockTitle:UInt
		get() = m_TrackBlockTitle
		set(value){
			//DO NOT HARD-CODE THIS VALUE!!!!!  I MUST be set from the file or stream being read in.
			if(value != MIDI_TRK_VALUE) return

			m_TrackBlockTitle = value
		}

	var TrackIndex:Int
		get() = m_TrackIndex
		set(value){
			if(value < 0) return
			m_TrackIndex = value
		}

	operator fun get(idx:Int) = m_Events[idx]

	fun appendEvent(event:IEvent?)
	{
		//Realistically, this is where you'd want to execute the information
		//in the event so to minimize any additional processing.
		if( (event == null) || (processEvent(event) == false) )
			return

		m_Events.add(event)
	}

	private fun processEvent(event:IEvent) : Boolean
	{
		when(event.eMidiType) {
			TrackEventType.MIDI_EVENT ->
				return processMidiEvent(event as MidiChannelEvent)
			TrackEventType.SYSEX_EVENT ->
				return processSystemExclusionEvent(event as SysExclusionEvent)
			TrackEventType.META_EVENT ->
				return processMetaEvent(event as MetaEvent)
			TrackEventType.SYSREALTIME_EVENT ->
				return processSystemRealtimeEvent(event as SysRealtimeEvent)
		}
	}

	private fun processMidiEvent(event:IEvent) : Boolean
	{
		var bRetStat:Boolean = false

		when(event.midiEventIdentifier) {
			MidiMajorMessage.NOTE_ON ->
				bRetStat = processNoteOnEvent(event as MidiNoteOnEvent, true)
			MidiMajorMessage.NOTE_OFF ->
				bRetStat = processNoteOffEvent(event as MidiNoteOffEvent, false)
			MidiMajorMessage.CONTROL_CHANGE ->
				bRetStat = processControlChangeEvent(event as MidiControlChangeEvent)
			MidiMajorMessage.KEY_PRESSURE_AFTER_TOUCH ->
				bRetStat = processKeyPressureAfterTouch(event as MidiPolyphonicKeyPressureEvent)
			MidiMajorMessage.PROGRAM_CHANGE ->
				bRetStat = processProgramChange(event as MidiProgramChangeEvent)
			MidiMajorMessage.CHANNEL_PRESSURE_AFTER_TOUCH ->
				bRetStat = processChannelPressureAfterTouch(event as MidiChannelPressureEvent)
			MidiMajorMessage.PITCH_WHEEL_CHANGE ->
				bRetStat = processPitchWheelChange(event as MidiPitchWheelEvent)
			else ->
			{}
		}

		return bRetStat
	}

	private fun processMetaEvent(event:MetaEvent) : Boolean
	{
		return true
	}

	private fun processSystemExclusionEvent(event:SysExclusionEvent) : Boolean
	{
		return true
	}

	private fun processSystemRealtimeEvent(event:SysRealtimeEvent) : Boolean
	{
		return true
	}

	private fun processNoteOnEvent(event:MidiNoteOnEvent, noteOn:Boolean) : Boolean
	{
		if(noteOn == false) {
			Printer.printMessage("Note On message consistency error")
			return false
		}

		Printer.printUInt8AsHex(event.musicalNote.num)

		return true
	}

	private fun processNoteOffEvent(event:MidiNoteOffEvent, noteOn:Boolean) : Boolean
	{
		if(noteOn == true) {
			Printer.printMessage("Note off message consistency error")
			return false
		}

		Printer.printUInt8AsHex(event.musicalNote.num)

		return true
	}

	private fun processPitchWheelChange(event:MidiPitchWheelEvent) : Boolean
	{
		Printer.printUInt16AsHex(event.pitchWheelChange)

		return true
	}

	private fun processChannelPressureAfterTouch(event:MidiChannelPressureEvent) : Boolean
	{
		Printer.printUInt8AsHex(event.pressure)

		return true
	}

	private fun processProgramChange(event:MidiProgramChangeEvent) : Boolean
	{
		Printer.printUInt8AsHex(event.programNumber)

		return true
	}

	private fun processKeyPressureAfterTouch(event:MidiPolyphonicKeyPressureEvent) : Boolean
	{
		Printer.printUInt8AsHex(event.musicalNote.num)
		Printer.printUInt8AsHex(event.pressure)

		return true
	}

	private fun processControlChangeEvent(event:MidiControlChangeEvent) : Boolean
	{
		val MAX_CHANNEL:UByte = 0x0Fu //[0 - 15] for a total of 16 channels

		val MODE_LOCAL_CONTROL_CHK:UByte = 0x7Au //122
		val MODE_ALL_NOTES_CHK:UByte = 0x7Bu //123
		val MODE_OMNI_MODE_ON:UByte = 0x7Du //125
		val MODE_OMNI_MODE_OFF:UByte = 0x7Cu //124
		val MODE_MONO_MODE_ON:UByte = 0x7Eu //126
		val MODE_MONO_MODE_OFF:UByte = 0x7Fu //127

		var numberOfChannels:UByte = 0u

		var ctrlModeState:MidiEventModeControlStates = MidiEventModeControlStates.ALL_NOTES_OFF

		//Validate for those messages which have a channel - This will actually be most of them
		if(event.channel!! > MAX_CHANNEL) return false

		//guard let changeValue = event.controllerChangeValue else {return false}

		when(event.controllerNumber) {
			MODE_LOCAL_CONTROL_CHK ->
				ctrlModeState = if(event.controllerChangeValue.toUInt() == 0u)  MidiEventModeControlStates.LOCAL_CONTROL_OFF else MidiEventModeControlStates.LOCAL_CONTROL_ON
			MODE_ALL_NOTES_CHK ->
				ctrlModeState = if(event.controllerChangeValue.toUInt() == 0u) MidiEventModeControlStates.ALL_NOTES_OFF else MidiEventModeControlStates.UNDEFINED
			MODE_OMNI_MODE_ON ->
				ctrlModeState = if(event.controllerChangeValue.toUInt() == 0u) MidiEventModeControlStates.OMNI_MODE_ON else MidiEventModeControlStates.UNDEFINED
			MODE_OMNI_MODE_OFF ->
				ctrlModeState = if(event.controllerChangeValue.toUInt() == 0u) MidiEventModeControlStates.OMNI_MODE_OFF else MidiEventModeControlStates.UNDEFINED
			MODE_MONO_MODE_ON -> {
				ctrlModeState = MidiEventModeControlStates.MONO_MODE_ON
				numberOfChannels = event.controllerChangeValue
				}
			MODE_MONO_MODE_OFF ->
				ctrlModeState = MidiEventModeControlStates.MONO_MODE_OFF
			else ->
				return processStandardControlMessage(event) //Need to change the parameter type or make a separate call
		}

		Printer.printUInt8AsHex(numberOfChannels)
		//Printer.printUInt8AsHex(ctrlModeState.num)

		return true
	}


	private fun processStandardControlMessage(event:MidiControlChangeEvent) : Boolean
	{
//		guard let controlNumber:UInt8 = event.controllerNumber else {return false}
//		guard let controlValue:UInt8 = event.controllerChangeValue else {return false}
//
//		//Don't set this to an enum in the event struct during the initial processing of the message
//		//since, depending on the control message type - the controllerNumber can have different meanings.
//		var controlDevideID:MidiControllerMessage? = MidiControllerMessage(rawValue: controlNumber)
//
//		guard controlDevideID != nil else {return false}
//
//		Printer.printUInt8AsHex(X: controlValue)

		return true
	}
}