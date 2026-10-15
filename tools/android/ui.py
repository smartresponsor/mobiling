#!/usr/bin/env python3
"""Current access-flow UI automation: locate nodes by text/bounds, never fixed taps."""
import os
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

if len(sys.argv) < 3:
    sys.exit('Usage: ui.py SERIAL {smoke|open-signin|tap-text TEXT|login}')
serial, action, *args = sys.argv[1:]
def adb(*cmd):
    try:
        result = subprocess.run(['adb', '-s', serial, *cmd], capture_output=True, text=True, timeout=35)
    except subprocess.TimeoutExpired:
        raise RuntimeError('adb command timed out') from None
    if result.returncode:
        raise RuntimeError(f'adb command failed (exit {result.returncode})')
    return result.stdout

def dump():
    for attempt in range(3):
        adb('shell', 'uiautomator', 'dump', '/sdcard/mobiling-ui.xml')
        root = ET.fromstring(adb('shell', 'cat', '/sdcard/mobiling-ui.xml'))
        if not any(n.get('text') == "System UI isn't responding" for n in root.iter('node')):
            return root
        wait = next((n for n in root.iter('node') if n.get('resource-id') == 'android:id/aerr_wait' and n.get('enabled') == 'true'), None)
        bounds = re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]', wait.get('bounds', '')) if wait is not None else None
        if not bounds:
            raise RuntimeError('System UI ANR has no enabled Wait action; run diagnose.')
        x1, y1, x2, y2 = map(int, bounds.groups())
        adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
        time.sleep(2)
    raise RuntimeError('System UI ANR persisted after bounded recovery; run diagnose.')

def texts(root):
    return [n.get('text') for n in root.iter('node') if n.get('text')]

def expect(text):
    for _ in range(8):
        root = dump()
        if text in texts(root):
            return root
        time.sleep(1)
    raise RuntimeError(f'Expected {text!r}' + ('' if action == 'login' else f'; visible: {texts(root)}'))

def tap_text(text):
    root = expect(text)
    parents = {c: p for p in root.iter() for c in p}
    matches = [n for n in root.iter('node') if n.get('text') == text]
    for target in matches:
        while target.get('clickable') != 'true' and target in parents:
            target = parents[target]
        if target.tag == 'node' and target.get('enabled') == 'true':
            bounds = re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]', target.get('bounds', ''))
            if bounds:
                x1, y1, x2, y2 = map(int, bounds.groups())
                adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
                time.sleep(1)
                return
    raise RuntimeError(f'No enabled clickable node for {text!r}')

def open_signin():
    root = dump()
    if 'Email' not in texts(root):
        tap_text('Sign in')
    expect('Email'); expect('Password')

try:
    if action == 'ready':
        dump()
        print('Android UI readiness: OK')
    elif action == 'tap-text':
        if len(args) != 1:
            raise RuntimeError('Usage: tap-text TEXT')
        tap_text(args[0])
    elif action == 'open-signin':
        open_signin()
    elif action == 'smoke':
        expect('Guest entry')
        open_signin()
        tap_text('Recover access')
        expect('Email'); expect('Send recovery code')
        tap_text('Return to access welcome')
        # Recovery's actual onBack returns to SignIn, despite the shared label.
        expect('Password')
        tap_text('Return to access welcome')
        expect('Guest entry')
        tap_text('Create access')
        expect('Company name'); expect('Password')
        tap_text('Return to access welcome')
        expect('Guest entry')
        print('PASS: welcome -> sign in -> recovery -> sign in -> welcome -> create access -> welcome')
    elif action == 'login':
        # Explicit opt-in only; never invent accounts or record credentials in captures.
        email = os.environ.get('MOBILING_LOGIN_EMAIL', '')
        password = os.environ.get('MOBILING_LOGIN_PASSWORD', '')
        if not email or not password:
            raise RuntimeError('Set MOBILING_LOGIN_EMAIL and MOBILING_LOGIN_PASSWORD for an existing test account.')
        if not re.fullmatch(r'[A-Za-z0-9@._+!-]+', email) or not re.fullmatch(r'[A-Za-z0-9@._+!-]+', password):
            raise RuntimeError('adb text input supports only simple ASCII credentials here; enter other credentials manually.')
        open_signin()
        tap_text('Email'); adb('shell', 'input', 'text', email)
        tap_text('Password'); adb('shell', 'input', 'text', password)
        adb('shell', 'input', 'keyevent', 'KEYCODE_BACK')
        tap_text('Sign in')
        expect('Mobile dashboard')
        print('Login reached Mobile dashboard.')
    else:
        raise RuntimeError('Expected smoke, open-signin, tap-text TEXT, or login.')
except (RuntimeError, subprocess.SubprocessError, ET.ParseError) as error:
    print(f'UI automation failed: {error}', file=sys.stderr)
    sys.exit(1)
