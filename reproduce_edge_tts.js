import { TTSService } from './services/ttsService.js';
import fs from 'fs';
import path from 'path';

const mockDb = {
  tts_profiles: [],
  tts_cache: [],
  tts_sets: [],
  language_phrases: [],
  tts_enabled_voices: [],
  edge_voice_catalog: { voices: [] }
};

const ttsService = new TTSService(mockDb);

async function testEdgeTTS() {
  try {
    console.log('Testing Edge TTS...');
    const text = 'Hello world, this is a test of Edge TTS.';
    const language = 'en-US';
    const voiceSelection = {
      provider: 'edge',
      voiceName: 'en-US-AndrewMultilingualNeural'
    };

    const result = await ttsService.generateTTS(text, language, voiceSelection, 'questions');
    console.log('Success:', result);
  } catch (error) {
    console.error('FAILED:', error);
  }
}

async function testEdgeTTSWithProfile() {
  try {
    console.log('\nTesting Edge TTS with Profile...');
    const text = 'Hello world, this is a test of Edge TTS with profile settings.';
    const language = 'en-US';
    
    const profile = {
      id: 'profile_test',
      provider: 'edge',
      voice: 'en-US-AndrewMultilingualNeural',
      speaking_rate: 1.2,
      pitch: 5,
      volume_gain_db: 10
    };
    
    mockDb.tts_profiles.push(profile);

    const voiceSelection = {
      profileId: 'profile_test'
    };

    const result = await ttsService.generateTTS(text, language, voiceSelection, 'questions');
    console.log('Success with profile:', result);
  } catch (error) {
    console.error('FAILED with profile:', error);
  }
}

async function runTests() {
  await testEdgeTTS();
  await testEdgeTTSWithProfile();
}

runTests();
