"""
Reports module.
Converts analytics results into various output formats.
No database access, no calculations - pure output generation.
"""

from typing import List, Dict, Any, Optional
import csv
import io
from datetime import datetime


class ReportGenerator:
    """Generates reports in various formats from analytics data."""
    
    @staticmethod
    def generate_csv(data: List[Dict[str, Any]], filename: str = "report.csv") -> str:
        """
        Generate CSV report from data.
        
        Args:
            data: List of dictionaries to convert to CSV
            filename: Output filename
            
        Returns:
            CSV file path
        """
        if not data:
            print(f"[REPORT] No data to generate CSV: {filename}")
            return ""
        
        # Flatten nested dictionaries
        flattened_data = [ReportGenerator._flatten_dict(row) for row in data]
        
        # Write to CSV
        output = io.StringIO()
        if flattened_data:
            writer = csv.DictWriter(output, fieldnames=flattened_data[0].keys())
            writer.writeheader()
            writer.writerows(flattened_data)
        
        csv_content = output.getvalue()
        output.close()
        
        # Write to file
        filepath = f"/mnt/user-data/outputs/{filename}"
        with open(filepath, 'w', newline='') as f:
            f.write(csv_content)
        
        print(f"[REPORT] CSV generated: {filepath}")
        return filepath
    
    @staticmethod
    def _flatten_dict(d: Dict[str, Any], parent_key: str = '', sep: str = '_') -> Dict[str, Any]:
        """Flatten nested dictionary."""
        items = []
        for k, v in d.items():
            new_key = f"{parent_key}{sep}{k}" if parent_key else k
            if isinstance(v, dict):
                items.extend(ReportGenerator._flatten_dict(v, new_key, sep=sep).items())
            elif isinstance(v, list):
                items.append((new_key, str(v)))
            else:
                items.append((new_key, v))
        return dict(items)
    
    @staticmethod
    def generate_table(data: List[Dict[str, Any]], title: str = "Report") -> str:
        """
        Generate formatted text table from data.
        
        Args:
            data: List of dictionaries
            title: Table title
            
        Returns:
            Formatted table string
        """
        if not data:
            return f"\n{title}\n{'='*50}\nNo data available\n"
        
        # Get all unique keys
        keys = list(data[0].keys())
        
        # Calculate column widths
        col_widths = {key: len(str(key)) for key in keys}
        for row in data:
            for key in keys:
                value_len = len(str(row.get(key, '')))
                col_widths[key] = max(col_widths[key], value_len)
        
        # Build table
        output = []
        output.append(f"\n{title}")
        output.append("=" * (sum(col_widths.values()) + 3 * len(keys) + 1))
        
        # Header
        header = " | ".join(str(key).ljust(col_widths[key]) for key in keys)
        output.append(header)
        output.append("-" * (sum(col_widths.values()) + 3 * len(keys) + 1))
        
        # Rows
        for row in data:
            row_str = " | ".join(str(row.get(key, '')).ljust(col_widths[key]) for key in keys)
            output.append(row_str)
        
        output.append("=" * (sum(col_widths.values()) + 3 * len(keys) + 1))
        output.append(f"Total Records: {len(data)}\n")
        
        return "\n".join(output)
    
    @staticmethod
    def generate_summary_report(analytics_results: Dict[str, Any], 
                               report_type: str = "Student") -> str:
        """
        Generate comprehensive summary report.
        
        Args:
            analytics_results: Dictionary of analytics results
            report_type: Type of report (Student/Teacher/Subject)
            
        Returns:
            Formatted summary report string
        """
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        
        output = []
        output.append("=" * 80)
        output.append(f"{report_type} Analytics Report".center(80))
        output.append(f"Generated: {timestamp}".center(80))
        output.append("=" * 80)
        output.append("")
        
        # Recursively format the results
        for section, content in analytics_results.items():
            output.append(f"\n{section.upper().replace('_', ' ')}")
            output.append("-" * 80)
            output.append(ReportGenerator._format_content(content))
            output.append("")
        
        output.append("=" * 80)
        output.append("End of Report")
        output.append("=" * 80)
        
        return "\n".join(output)
    
    @staticmethod
    def _format_content(content: Any, indent: int = 0) -> str:
        """Recursively format content for display."""
        prefix = "  " * indent
        
        if isinstance(content, dict):
            lines = []
            for key, value in content.items():
                if isinstance(value, (dict, list)):
                    lines.append(f"{prefix}{key}:")
                    lines.append(ReportGenerator._format_content(value, indent + 1))
                else:
                    lines.append(f"{prefix}{key}: {value}")
            return "\n".join(lines)
        
        elif isinstance(content, list):
            if not content:
                return f"{prefix}(No data)"
            
            # If list of dicts, format as mini-table
            if all(isinstance(item, dict) for item in content):
                lines = []
                for i, item in enumerate(content, 1):
                    lines.append(f"{prefix}[{i}]")
                    lines.append(ReportGenerator._format_content(item, indent + 1))
                return "\n".join(lines)
            else:
                return "\n".join(f"{prefix}- {item}" for item in content)
        
        else:
            return f"{prefix}{content}"
    
    @staticmethod
    def save_report_to_file(content: str, filename: str) -> str:
        """
        Save report content to file.
        
        Args:
            content: Report content
            filename: Output filename
            
        Returns:
            File path
        """
        filepath = f"/mnt/user-data/outputs/{filename}"
        
        with open(filepath, 'w') as f:
            f.write(content)
        
        print(f"[REPORT] Report saved: {filepath}")
        return filepath
    
    @staticmethod
    def generate_student_report(student_id: int, performance: Dict[str, Any], 
                               semester_trend: Dict[str, Any], 
                               subject_analysis: Dict[str, Any],
                               weak_subjects: Dict[str, Any]) -> str:
        """
        Generate comprehensive student performance report.
        
        Args:
            student_id: Student ID
            performance: Performance report data
            semester_trend: Semester trend analysis
            subject_analysis: Subject performance analysis
            weak_subjects: Weak subjects identification
            
        Returns:
            Formatted report string
        """
        report = {
            'Student ID': student_id,
            'Student Information': performance,
            'Semester-wise Performance Trend': semester_trend,
            'Subject Performance Analysis': subject_analysis,
            'Areas Needing Improvement': weak_subjects
        }
        
        return ReportGenerator.generate_summary_report(report, "Student Performance")
    
    @staticmethod
    def generate_teacher_report(teacher_id: int, performance: Dict[str, Any],
                               students: Dict[str, Any], doubts: Dict[str, Any],
                               insights: Dict[str, Any]) -> str:
        """
        Generate comprehensive teacher performance report.
        
        Args:
            teacher_id: Teacher ID
            performance: Teacher performance metrics
            students: Student distribution data
            doubts: Doubt handling metrics
            insights: Teaching insights
            
        Returns:
            Formatted report string
        """
        report = {
            'Teacher ID': teacher_id,
            'Performance Metrics': performance,
            'Student Distribution': students,
            'Doubt Resolution': doubts,
            'Teaching Insights & Recommendations': insights
        }
        
        return ReportGenerator.generate_summary_report(report, "Teacher Performance")
    
    @staticmethod
    def generate_subject_report(subject_name: str, difficulty: Dict[str, Any],
                               distribution: Dict[str, Any], pass_fail: Dict[str, Any],
                               recommendations: Dict[str, Any]) -> str:
        """
        Generate comprehensive subject analysis report.
        
        Args:
            subject_name: Subject name
            difficulty: Difficulty analysis
            distribution: Score distribution
            pass_fail: Pass/fail ratio analysis
            recommendations: Subject recommendations
            
        Returns:
            Formatted report string
        """
        report = {
            'Subject Name': subject_name,
            'Difficulty Analysis': difficulty,
            'Score Distribution': distribution,
            'Pass/Fail Analysis': pass_fail,
            'Recommendations': recommendations
        }
        
        return ReportGenerator.generate_summary_report(report, "Subject Analysis")
    
    @staticmethod
    def generate_comparative_report(comparison_data: Dict[str, Any],
                                   comparison_type: str = "Students") -> str:
        """
        Generate comparative analysis report.
        
        Args:
            comparison_data: Comparison results
            comparison_type: What is being compared
            
        Returns:
            Formatted comparison report
        """
        return ReportGenerator.generate_summary_report(
            comparison_data, 
            f"Comparative Analysis - {comparison_type}"
        )
    
    @staticmethod
    def generate_dashboard_summary(overall_stats: Dict[str, Any]) -> str:
        """
        Generate high-level dashboard summary.
        
        Args:
            overall_stats: Overall statistics
            
        Returns:
            Dashboard summary string
        """
        output = []
        output.append("\n" + "=" * 80)
        output.append("ACADEMIC ANALYTICS DASHBOARD".center(80))
        output.append("=" * 80)
        output.append("")
        
        # Key metrics
        for category, metrics in overall_stats.items():
            output.append(f"\n{category.upper()}")
            output.append("-" * 40)
            
            if isinstance(metrics, dict):
                for key, value in metrics.items():
                    output.append(f"  {key}: {value}")
            else:
                output.append(f"  {metrics}")
        
        output.append("\n" + "=" * 80)
        
        return "\n".join(output)
    
    @staticmethod
    def export_analytics_bundle(student_reports: List[str] = None,
                               teacher_reports: List[str] = None,
                               subject_reports: List[str] = None,
                               output_dir: str = "/mnt/user-data/outputs") -> Dict[str, Any]:
        """
        Export complete analytics bundle.
        
        Args:
            student_reports: List of student report contents
            teacher_reports: List of teacher report contents
            subject_reports: List of subject report contents
            output_dir: Output directory
            
        Returns:
            Dictionary with file paths
        """
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        files_created = {}
        
        if student_reports:
            for i, report in enumerate(student_reports, 1):
                filename = f"student_report_{i}_{timestamp}.txt"
                filepath = ReportGenerator.save_report_to_file(report, filename)
                files_created[f'student_{i}'] = filepath
        
        if teacher_reports:
            for i, report in enumerate(teacher_reports, 1):
                filename = f"teacher_report_{i}_{timestamp}.txt"
                filepath = ReportGenerator.save_report_to_file(report, filename)
                files_created[f'teacher_{i}'] = filepath
        
        if subject_reports:
            for i, report in enumerate(subject_reports, 1):
                filename = f"subject_report_{i}_{timestamp}.txt"
                filepath = ReportGenerator.save_report_to_file(report, filename)
                files_created[f'subject_{i}'] = filepath
        
        print(f"[REPORT] Analytics bundle exported: {len(files_created)} files")
        return files_created
