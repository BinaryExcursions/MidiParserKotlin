class MidiTrack
{
	private var m_TrackBlockTitle:UInt = 0u
	private lateinit var m_Events:ArrayList<IEvent>

	var TrackBlockTitle:UInt
		get() = m_TrackBlockTitle
		set(value){
			//DO NOT HARD-CODE THIS VALUE!!!!!  I MUST be set from the file or stream being read in.
			if(value != MIDI_TRK_VALUE) return

			m_TrackBlockTitle = value
		}

	operator fun get(idx:Int) = m_Events[idx]

	fun appendEvent(event:IEvent?)
	{
		if(event == null) return

		m_Events.add(event!!)
	}
}