import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { AgentDashboardComponent } from './components/agent-dashboard/agent-dashboard.component';
import { ClientDashboardComponent } from './components/client-dashboard/client-dashboard.component';
import { ChatroomComponent } from './components/chatroom/chatroom.component';
import { authGuard } from './guards/auth.guard';
import { clientGuard } from './guards/client.guard';
import { agentGuard } from './guards/agent.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { 
    path: 'client/dashboard', 
    component: ClientDashboardComponent,
    canActivate: [clientGuard]
  },
  { 
    path: 'agent/dashboard', 
    component: AgentDashboardComponent,
    canActivate: [agentGuard]
  },
  { 
    path: 'chatroom/:ticketId', 
    component: ChatroomComponent,
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: '/login' }
];
