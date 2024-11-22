package com.byagowi.persiancalendar.ui.calendar

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_EVENTS
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.holidayString
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.AskForCalendarPermissionDialog
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getShiftWorksInDaysDistance
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.readDayDeviceEvents

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.EventsTab(
    navigateToHolidaysSettings: () -> Unit,
    viewModel: CalendarViewModel,
    animatedContentScope: AnimatedContentScope,
    bottomPadding: Dp,
    navigateToWeek: (Jdn) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))

        val jdn by viewModel.selectedDay.collectAsState()
        val refreshToken by viewModel.refreshToken.collectAsState()
        val shiftWorkTitle = remember(jdn, refreshToken) { getShiftWorkTitle(jdn) }
        AnimatedVisibility(visible = shiftWorkTitle != null) {
            AnimatedContent(
                targetState = shiftWorkTitle ?: "",
                label = "shift work title",
                transitionSpec = appCrossfadeSpec,
            ) { state ->
                SelectionContainer {
                    Text(
                        state,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    )
                }
            }
        }
        val shiftWorkInDaysDistance = getShiftWorksInDaysDistance(jdn)
        AnimatedVisibility(visible = shiftWorkInDaysDistance != null) {
            AnimatedContent(
                targetState = shiftWorkInDaysDistance ?: "",
                label = "shift work days diff",
                transitionSpec = appCrossfadeSpec,
            ) { state ->
                SelectionContainer {
                    Text(
                        state,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Column(Modifier.padding(horizontal = 24.dp)) {
            val events = readEvents(jdn, refreshToken)
            Spacer(Modifier.height(16.dp))
            AnimatedVisibility(events.isEmpty()) {
                Text(
                    stringResource(R.string.no_event),
                    Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
            Column(
                Modifier.sharedBounds(
                    rememberSharedContentState(SHARED_CONTENT_KEY_EVENTS),
                    animatedVisibilityScope = animatedContentScope,
                )
            ) {
                DayEvents(events) { viewModel.refreshCalendar() }
                if (events.any { it is CalendarEvent.DeviceCalendarEvent && it.time != null }) {
                    val title = stringResource(R.string.week_view)
                    @OptIn(ExperimentalMaterial3Api::class)
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(stringResource(R.string.week_view)) } },
                        state = rememberTooltipState()
                    ) {
                        Box(
                            Modifier
                                .padding(top = 4.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { navigateToWeek(jdn) }
                                .padding(all = 12.dp),
                        ) {
                            Icon(
                                Icons.Default.Timelapse,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp),
                                contentDescription = title,
                            )
                        }
                    }
                }
            }
        }

        val language by language.collectAsState()
        val context = LocalContext.current
        if (PREF_HOLIDAY_TYPES !in context.preferences && language.isIranExclusive) {
            Spacer(modifier = Modifier.height(16.dp))
            EncourageActionLayout(
                header = stringResource(R.string.warn_if_events_not_set),
                discardAction = {
                    context.preferences.edit {
                        putStringSet(PREF_HOLIDAY_TYPES, EventsRepository.iranDefault)
                    }
                },
                acceptAction = navigateToHolidaysSettings,
            )
        } else if (PREF_SHOW_DEVICE_CALENDAR_EVENTS !in context.preferences) {
            var showDialog by remember { mutableStateOf(false) }
            if (showDialog) AskForCalendarPermissionDialog { showDialog = false }

            Spacer(modifier = Modifier.height(16.dp))
            EncourageActionLayout(
                header = stringResource(R.string.ask_for_calendar_permission),
                discardAction = {
                    context.preferences.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false) }
                },
                acceptButton = stringResource(R.string.yes),
                acceptAction = { showDialog = true },
            )
        }

        // Events addition fab placeholder, so events can be scrolled after it
        Spacer(Modifier.height(76.dp))
        Spacer(Modifier.height(bottomPadding))
    }
}

@Composable
fun ColumnScope.DayEvents(events: List<CalendarEvent<*>>, refreshCalendar: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ViewEventContract()) { refreshCalendar() }
    events.forEach { event ->
        val backgroundColor by animateColor(
            when {
                event is CalendarEvent.DeviceCalendarEvent -> {
                    runCatching {
                        // should be turned to long then int otherwise gets stupid alpha
                        if (event.color.isEmpty()) null else Color(event.color.toLong())
                    }.onFailure(logException).getOrNull() ?: MaterialTheme.colorScheme.primary
                }

                event.isHoliday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )

        val eventTime = (event as? CalendarEvent.DeviceCalendarEvent)?.time?.let { "\n" + it } ?: ""
        AnimatedContent(
            (if (event.isHoliday) language.value.inParentheses.format(
                event.title, holidayString
            ) else event.title) + eventTime,
            label = "event title",
            transitionSpec = appCrossfadeSpec,
        ) { title ->
            Row(
                @OptIn(ExperimentalFoundationApi::class) Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(backgroundColor)
                    .combinedClickable(
                        enabled = event is CalendarEvent.DeviceCalendarEvent,
                        onClick = {
                            if (event is CalendarEvent.DeviceCalendarEvent) {
                                runCatching { launcher.launch(event.id) }
                                    .onFailure {
                                        Toast
                                            .makeText(
                                                context,
                                                R.string.device_does_not_support,
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                    .onFailure(logException)
                            }
                        },
                    )
                    .padding(8.dp)
                    .semantics {
                        this.contentDescription = if (event.isHoliday) context.getString(
                            R.string.holiday_reason, event.oneLinerTitleWithTime
                        ) else event.oneLinerTitleWithTime
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val contentColor by animateColor(
                    if (backgroundColor.isLight) Color.Black else Color.White,
                )
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    SelectionContainer {
                        Text(
                            title,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                AnimatedVisibility(event is CalendarEvent.DeviceCalendarEvent) {
                    Icon(
                        Icons.AutoMirrored.Default.OpenInNew,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun readEvents(jdn: Jdn, refreshToken: Int): List<CalendarEvent<*>> {
    val context = LocalContext.current
    val events = remember(jdn, refreshToken) {
        (eventsRepository?.getEvents(jdn, context.readDayDeviceEvents(jdn))
            ?: emptyList()).sortedBy {
            when {
                it.isHoliday -> 0L
                it !is CalendarEvent.DeviceCalendarEvent -> 1L
                else -> it.start.time
            }
        }
    }
    return events
}

private class ViewEventContract : ActivityResultContract<Long, Void?>() {
    override fun parseResult(resultCode: Int, intent: Intent?) = null
    override fun createIntent(context: Context, input: Long): Intent {
        return Intent(Intent.ACTION_VIEW).setData(
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, input)
        )
    }
}
